package com.catchingnow.robfuscate

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * @author heruoxin @ CatchingNow Inc.
 * @since 2019-03-20
 */
class RoPlugin implements Plugin<Project> {

    void apply(Project project) {
        def isApp = project.plugins.hasPlugin(AppPlugin)
        if (isApp) {
            def android = project.extensions.findByType(AppExtension)
            android.registerTransform(new RoTransform(project))
        }
    }
}