#! /bin/bash

# 打 jar 包
# bash ci.sh package

# 构建 docker 镜像
# bash ci.sh build_docker [version]

# set -ex

DIST_DIR=.dist
VERSION=${2:-latest}

package() {
    echo "应用打包开始"
    mkdir -p ${DIST_DIR}
    mvn clean package -U -DskipTests=true
    cp target/*.jar ${DIST_DIR}/app.jar
    cp script/run.sh ${DIST_DIR}/run.sh
    chmod 755 ${DIST_DIR}/*.sh
    ls -la ${DIST_DIR}
    echo "应用打包结束"
}

build_docker() {
    package
    echo "镜像构建开始 ${VERSION}"
    export DOCKER_SCAN_SUGGEST=false
    sudo docker build -t fengjx/sentinel-dashboard-apollo:${VERSION} .
    echo "镜像构建结束"
}

build_docker_dev() {
    package
    echo '镜像构建开始 ${VERSION}'
    export DOCKER_SCAN_SUGGEST=false
    sudo docker build -t fengjx/sentinel-dashboard-apollo:dev .
    echo "镜像构建结束"
}

push_docker() {
    package
    echo "镜像构建并推送开始 ${VERSION}"
    sudo docker buildx build --platform linux/amd64,linux/arm64 -t fengjx/sentinel-dashboard-apollo:${VERSION} --push .
    echo "镜像构建并推送结束 ${VERSION}"
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
build_docker_dev)
    build_docker_dev "$@"
    ;;
push_docker)
    push_docker "$@"
    ;;
esac
