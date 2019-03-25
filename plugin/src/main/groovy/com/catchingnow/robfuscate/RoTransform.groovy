package com.catchingnow.robfuscate

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.google.common.collect.Sets
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

class RoTransform extends Transform{
    
    boolean hasPutConverter = false
    Project project
    public RoTransform(Project project) {
        this.project = project
    }

    @Override
    String getName() {
        return "robfuscate"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<QualifiedContent.Scope> getScopes() {
        return Sets.immutableEnumSet(QualifiedContent.Scope.PROJECT);
    }

    @Override
    boolean isIncremental() {
        return true
    }

    @Override
    void transform(Context context, Collection<TransformInput> inputs,
                   Collection<TransformInput> referencedInputs,
                   TransformOutputProvider outputProvider, boolean isIncremental)
            throws IOException, TransformException, InterruptedException {
        // Transform的inputs有两种类型，一种是目录，一种是jar包，要分开遍历
        inputs.each {TransformInput input ->
            //对类型为“文件夹”的input进行遍历
            input.directoryInputs.each {DirectoryInput directoryInput->
                //文件夹里面包含的是我们手写的类以及R.class、BuildConfig.class以及R$XXX.class等

                // 获取output目录
                def dest = outputProvider.getContentLocation(directoryInput.name,
                        directoryInput.contentTypes, directoryInput.scopes,
                        Format.DIRECTORY)

                // 修改 R 文件
                RoInject.injectDir(project, directoryInput.file.absolutePath);

                // 放入解码类
                if (!hasPutConverter) {
                    hasPutConverter = true
                    def s = File.separator
                    def f = new File("${directoryInput.file.absolutePath}${s}${RobfuscateUtilDump.NAME.replace("/", s)}.class")
                    f.mkdirs()
                    f.delete()
                    def os = f.newOutputStream()
                    os.write(RobfuscateUtilDump.dump())
                    os.close()
                }

                // 将input的目录复制到output指定目录
                FileUtils.copyDirectory(directoryInput.file, dest)

            }
            //对类型为jar文件的input进行遍历
            input.jarInputs.each {JarInput jarInput->

                //jar文件一般是第三方依赖库jar文件

                // 重命名输出文件（同目录copyFile会冲突）
                def jarName = jarInput.name
                def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
                if(jarName.endsWith(".jar")) {
                    jarName = jarName.substring(0,jarName.length()-4)
                }
                //生成输出路径
                def dest = outputProvider.getContentLocation(jarName+md5Name,
                        jarInput.contentTypes, jarInput.scopes, Format.JAR)
                //将输入内容复制到输出
                FileUtils.copyFile(jarInput.file, dest)
            }
        }
    }
}
