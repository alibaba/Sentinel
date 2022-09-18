#! /bin/bash

# 打 jar 包
# bash ci.sh package

# 构建 docker 镜像
# bash ci.sh build_docker [version]

# set -ex

DIST_DIR=.dist
VERSION=${2:-latest}

package() {
    echo '应用打包开始'
    mkdir -p ${DIST_DIR}
    mvn clean package -U -DskipTests=true
    cp target/*.jar ${DIST_DIR}/app.jar
    cp script/run.sh ${DIST_DIR}/run.sh
    chmod 755 ${DIST_DIR}/*.sh
    ls -la ${DIST_DIR}
    echo '应用打包结束'
}

build_docker() {
    package
    echo '镜像构建开发'
    export DOCKER_SCAN_SUGGEST=false
    docker build -t fengjx/sentinel-dashboard:${VERSION} .
    echo '镜像构建结束'
}

case_opt=$1
shift

case ${case_opt} in
package)
    package "$@"
    ;;
build_docker)
    build_docker "$@"
    ;;
esac
