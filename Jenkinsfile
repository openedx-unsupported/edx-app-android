#!/usr/bin/env groovy

pipeline {
    agent any 

    environment {
            ANDROID_HOME = '/opt/android-sdk-linux'
            EDX_PROPERTIES = './OpenEdXMobile/edx.properties'
    }

    stages {
        
        stage('create required file '){
           steps {
               touch file: '$EDX_PROPERTIES'
               writeFile file: '$EDX_PROPERTIES', text: 'edx.dir = \'../../edx-mobile-config/prod/\''           
               } 
        }
        stage('compiling edx-app-android') {
            steps {
                sh 'chmod 775 ./compile_android.sh'
                sh './compile_android.sh'
            }
        }
    }
}
