#!/bin/bash
cd /Users/Shared/ON3/git/devops/jenkins/api || exit
./script_clone_job.sh common-rest s3-spring-boot-starter false
./add_job_to_view.sh 's3-spring-boot-starter' 'GedocFlex - MicroServices'
./build.sh 's3-spring-boot-starter'
