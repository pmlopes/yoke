#!/bin/sh
set -e

# clean any build left overs
#./gradlew clean

# gen framework docs
cd framework/src/main/java
docco-husky -name "Yoke Framework" README.md com
rm -Rf ../../../../../yoke-site/framework-java
mv docs ../../../../../yoke-site/framework-java
cd ../../../..

cd framework/src/main/groovy
docco-husky -name "Yoke Framework" README.md com
rm -Rf ../../../../../yoke-site/framework-groovy
mv docs ../../../../../yoke-site/framework-groovy
cd ../../../..

cd framework/src/main/resources
docco-husky -name "Yoke Framework" README.md com
rm -Rf ../../../../../yoke-site/framework-javascript
mv docs ../../../../../yoke-site/framework-javascript
cd ../../../..

#framework framewor-extras middleware/swagger engine/handlebars engine/jade engine/mvel engine/thymeleaf
#mv docs ../yoke-site/docs


#Making code changes
#Install the Git client for your operating system, and from your command line run
#
#git clone ssh://535ab5d65973ca023a000208@yoke-jetdrone.rhcloud.com/~/git/yoke.git/
#cd yoke/
#This will create a folder with the source code of your application. After making a change, add, commit, and push your changes.
#
#git add .
#git commit -m 'My changes'
#git push