const gulp = require('gulp');
const plugins = require('gulp-load-plugins')();
const open = require('open');
const app = {
  srcPath: 'app/', // 源代码
  devPath: 'tmp/', // 开发打包
  prdPath: 'dist/' // 生产打包
};

const JS_LIBS = [
  'node_modules/angular-ui-router/release/angular-ui-router.js',
  'node_modules/oclazyload/dist/ocLazyLoad.min.js',
  'node_modules/angular-loading-bar/build/loading-bar.min.js',
  'node_modules/angular-bootstrap/ui-bootstrap-tpls.min.js',
  'node_modules/moment/moment.js',
  'node_modules/angular-date-time-input/src/dateTimeInput.js',
  'node_modules/angularjs-bootstrap-datetimepicker/src/js/datetimepicker.js',
  'node_modules/angular-table-resize/dist/angular-table-resize.min.js',
  'node_modules/angular-clipboard/angular-clipboard.js',
  'node_modules/selectize/dist/js/standalone/selectize.js',
  'node_modules/angular-selectize2/dist/selectize.js',
  'node_modules/bootstrap-switch/dist/js/bootstrap-switch.min.js',
  'node_modules/ng-dialog/js/ngDialog.js',
  'node_modules/angular-ui-notification/dist/angular-ui-notification.min.js',
  'node_modules/angular-utils-pagination/dirPagination.js',
  'app/scripts/libs/treeTable.js',
];

const CSS_APP = [
  'node_modules/angular-loading-bar/build/loading-bar.min.css',
  'node_modules/bootstrap-switch/dist/css/bootstrap3/bootstrap-switch.css',
  'node_modules/ng-dialog/css/ngDialog.min.css',
  'node_modules/ng-dialog/css/ngDialog-theme-default.css',
  'node_modules/angularjs-bootstrap-datetimepicker/src/css/datetimepicker.css',
  'node_modules/angular-ui-notification/dist/angular-ui-notification.min.css',
  'node_modules/angular-table-resize/dist/angular-table-resize.css',
  'node_modules/selectize/dist/css/selectize.css',
  'app/styles/page.css',
  'app/styles/timeline.css',
  'app/styles/main.css'
];

const JS_APP = [
  'app/scripts/app.js',
  'app/scripts/filters/filters.js',
  'app/scripts/services/auth_service.js',
  'app/scripts/services/appservice.js',
  'app/scripts/services/flow_service_v1.js',
  'app/scripts/services/flow_service_v2.js',
  'app/scripts/services/degradeservice.js',
  'app/scripts/services/systemservice.js',
  'app/scripts/services/machineservice.js',
  'app/scripts/services/identityservice.js',
  'app/scripts/services/metricservice.js',
  'app/scripts/services/param_flow_service.js',
  'app/scripts/services/authority_service.js',
  'app/scripts/services/cluster_state_service.js',
  'app/scripts/services/gateway/api_service.js',
  'app/scripts/services/gateway/flow_service.js',
];

gulp.task('lib', function () {
  gulp.src(JS_LIBS)
    .pipe(plugins.concat('app.vendor.js'))
    .pipe(gulp.dest(app.devPath + 'js'))
    .pipe(plugins.uglify())
    .pipe(gulp.dest(app.prdPath + 'js'))
    .pipe(plugins.connect.reload());
});

/*
* css任务
* 在src下创建style文件夹，里面存放less文件。 
*/
gulp.task('css', function () {
  gulp.src(CSS_APP)
    .pipe(plugins.concat('app.css'))
    .pipe(gulp.dest(app.devPath + 'css'))
    .pipe(plugins.cssmin())
    .pipe(gulp.dest(app.prdPath + 'css'))
    .pipe(plugins.connect.reload());
});

/*
* js任务
* 在src目录下创建script文件夹，里面存放所有的js文件
*/
gulp.task('js', function () {
  gulp.src(JS_APP)
    .pipe(plugins.concat('app.js'))
    .pipe(gulp.dest(app.devPath + 'js'))
    .pipe(plugins.uglify())
    .pipe(gulp.dest(app.prdPath + 'js'))
    .pipe(plugins.connect.reload());
});

/*
* js任务
* 在src目录下创建script文件夹，里面存放所有的js文件
*/
gulp.task('jshint', function () {
  gulp.src(JS_APP)
    .pipe(plugins.jshint())
    .pipe(plugins.jshint.reporter());
});

// 每次发布的时候，可能需要把之前目录内的内容清除，避免旧的文件对新的容有所影响。 需要在每次发布前删除dist和build目录
gulp.task('clean', function () {
  gulp.src([app.devPath, app.prdPath])
    .pipe(plugins.clean());
});

// 总任务
gulp.task('build', ['clean', 'jshint', 'lib', 'js', 'css']);

// 服务
gulp.task('serve', ['build'], function () {
  plugins.connect.server({ //启动一个服务器
    root: [app.devPath], // 服务器从哪个路径开始读取，默认从开发路径读取
    livereload: true, // 自动刷新
    port: 1234
  });
  // 打开浏览器
  setTimeout(() => {
    open('http://localhost:8080/index_dev.htm')
  }, 200);
  // 监听
  gulp.watch(app.srcPath + '**/*.js', ['js']);
  gulp.watch(app.srcPath + '**/*.css', ['css']);
});

// 定义default任务
gulp.task('default', ['serve']);
