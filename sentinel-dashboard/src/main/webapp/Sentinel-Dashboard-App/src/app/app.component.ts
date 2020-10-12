import { Component, OnInit } from '@angular/core';
import { KieInfoService } from 'src/app/services/kie-info/kie-info.service';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  isCollapsed: boolean = false;
  projects: string[] = [];
  currentProject: string = '';
  environments: string[] = [];
  currentEnvir: string = '';
  services: any[];
  apps: any[];
  menuInfos: any[] = [];
  appPanels: any[];
  appfilter: string;

  constructor(
    private kieService: KieInfoService,
    private route: ActivatedRoute
  ) { }

  async ngOnInit() {
    this.isCollapsed = false;
    this.environments = ['', 'development', 'production', 'testing', 'acceptance'];
    this.currentEnvir = 'development';
    const _projects: any = await new Promise(resolve => {
      this.kieService.getProjects().subscribe(res => {
        if (res.success) {
          resolve(res);
        }
      });
    });
    this.projects = _projects.data;
    this.currentProject = this.projects[0];
    const _kieInfos: any = await new Promise(resolve => {
      this.kieService.getKieInfos(this.currentProject, this.currentEnvir).subscribe(res => {
        if (res.success) {
          resolve(res);
        }
      })
    });
    this.services = _kieInfos.data;
    this.apps = Array.from(new Set(_kieInfos.data.map(val => val.app)));
    this.initMenuInfos();
  }

  public projectChange($event) {
    this.kieService.getKieInfos(this.currentProject, this.currentEnvir).subscribe(res => {
      if (res.success) {
        this.apps = Array.from(new Set(res.data.map(val => val.app)));
        this.services = res.data;
        this.initMenuInfos();
      }
    });
    
  }

  public envirChange($event) {
    this.kieService.getKieInfos(this.currentProject, this.currentEnvir).subscribe(res => {
      if (res.success) {
        this.apps = Array.from(new Set(res.data.map(val => val.app)));  
        this.services = res.data;
        this.initMenuInfos();
      }
    });
  }

  public openHandler(menuInfo: any) {
    this.menuInfos.forEach(ele => {
      if (ele.app !== menuInfo.app) {
        ele.open = false;
      }
    });
  }

  private initMenuInfos() {
    this.menuInfos = [];
    this.apps.forEach(app => {
      var menuTmp = {
        app: app,
        open: false,
        serviceInfos: []
      };
      this.services.forEach(service => {
        if (service.app === menuTmp.app) {
          menuTmp.serviceInfos.push({
            open: false,
            service: service.service,
            version: service.serverVersion,
            id: service.id
          });
        }
      });
      this.menuInfos.push(menuTmp);
    });
    
    // 处理页面刷新时逻辑
    if (window.location.href.split('/').splice(4).length) {
      var nowApp = window.location.href.split('/').splice(4)[1];
      var nowServiceId = window.location.href.split('/').splice(4)[3];
      var flag = false;
      this.menuInfos.forEach(menuInfo => {
        if (menuInfo.app === nowApp) {
          menuInfo.open = true;
          menuInfo.serviceInfos.forEach(service => {
            if (service.id === nowServiceId) {
              service.open = true;
              flag = true;
            }
          });
        }
      });
      if (!flag) {
        window.location.replace('/#/home');
      }
    }
  }

}
