#!/usr/bin/env groovy

pipeline {
    agent {
     label 'android-worker'
    }

    environment {
            PIPELINE_JOBS_NAME = 'edx-app-android-pipeline'
            ANDROID_HOME = '/opt/android-sdk-linux'
            APK_PATH = 'OpenEdXMobile/build/outputs/apk/prod/debuggable'
            CONFIG_REPO_NAME = 'edx-mobile-config'
    }       

    stages {
        stage('checkingout configs') { 
            steps {
                sh 'mkdir -p edx-mobile-config'
                dir('edx-mobile-config'){
                    sshagent(credentials: ['jenkins-worker', 'jenkins-worker-pem'], ignoreMissing: true) {
                    checkout changelog: false, poll: false, scm: [
                        $class: 'GitSCM', 
                        branches: 
    // Using specific branch to avoid Firebase config limitations
                                //[[name: '*/master']],
                                [[name: 'naveed/automation_configs']],
                        doGenerateSubmoduleConfigurations: false, 
                        extensions: 
                                [[$class: 'CloneOption', honorRefspec: true,
                                    noTags: true, shallow: true]], 
                        submoduleCfg: [], 
                        userRemoteConfigs: 
                                    [[credentialsId: 'jenkins-worker',
                                    refspec: '+refs/heads/*:refs/remotes/origin/*', 
                                    url: "git@github.com:edx/${CONFIG_REPO_NAME}.git"]]
                            ]
                    }
                }
            }
        }
        stage('compiling edx-app-android') {
            steps {
                writeFile file: './OpenEdXMobile/edx.properties', text: 'edx.dir = \'../edx-mobile-config/prod/\''  
                sh 'bash ./resources/compile_android.sh'
            }
        }
        stage('valdiate compiled app') {
            steps {
                sh 'bash ./resources/validate_builds.sh'
            }
        }
        stage('archive the build') {
            steps {
                archiveArtifacts artifacts: "$APK_PATH/*.apk", onlyIfSuccessful: true
            }
        }
    }
} 
