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
            TEST_PROJECT_REPO_NAME = 'edx-app-test'
            AUT_NAME = 'edx-debuggable-2.23.2.apk'
            USER_NAME = credentials('AUTOMATION_USERNAME')
            USER_PASSWORD = credentials('AUTOMATION_PASSWORD')
    }

    stages {
        stage('checkingout configs') { 
            steps {
                dir("$CONFIG_REPO_NAME"){
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
                writeFile file: './OpenEdXMobile/edx.properties', text: 'edx.dir = \'../edx-mobile-config/stage/\''  
                sh 'bash ./resources/compile_android.sh'
            }
        }
        stage('valdiate compiled app') {
            steps {
                sh 'bash ./resources/validate_builds.sh'
                archiveArtifacts artifacts: "$APK_PATH/*.apk", onlyIfSuccessful: true
            }   
        }

        stage('checkout test project repo') {
            steps {
                dir("$TEST_PROJECT_REPO_NAME"){
                        checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[url: 'https://github.com/edx/edx-app-test.git']]])
                }
            }
        }
        stage('prepare package for aws device farm') {
            steps {
                sh 'bash ./resources/prepare_aws_package.sh'
                archiveArtifacts artifacts: "test_bundle.zip", onlyIfSuccessful: true                
            }
        }

        stage('setup virtual env and trigger run on aws device farm') {
            steps {
                sh 'bash ./resources/setup_virtual_env.sh'
            }
        }
    }
} 
