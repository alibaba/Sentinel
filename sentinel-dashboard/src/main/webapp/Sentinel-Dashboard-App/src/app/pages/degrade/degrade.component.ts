import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { NzButtonSize } from 'ng-zorro-antd/button';
import { KieDegradeService } from 'src/app/services/kie-degrade/kie-degrade.service';

@Component({
  selector: 'app-degrade',
  templateUrl: './degrade.component.html',
  styleUrls: ['./degrade.component.css']
})
export class DegradeComponent implements OnInit {
  app: string;
  service: string;
  service_id: string;
  btnSize: NzButtonSize = 'large';
  degradeRules: any[] = [];
  gradeModeMap: any = {};
  degradefilter: any;
  currentRule: any;

  // Modal 对话框显示
  isCreModVis: boolean = false;
  isDelModVis: boolean = false;
  isUpdModVis: boolean = false;

  constructor(
    private route: ActivatedRoute,
    private kieDegradeService: KieDegradeService
  ) { }

  ngOnInit(): void {
    this.mapInit();
    this.route.params.subscribe(res => {
      this.app = res.app;
      this.service = res.service;
      this.service_id = res.service_id;
    });
    this.queryDegradeRules();
  }

  private mapInit() {
    this.gradeModeMap = {
      0: '慢比例调用',
      1: '异常比例',
      2: '异常数'
    };
  }
  
  public queryDegradeRules() {
    this.kieDegradeService.queryKieDegradeRules(this.service_id).subscribe(res => {
      this.degradeRules = [];
      if (res.success) {
        if (res.data && res.data.length) {
          res.data.forEach(ele => {
            this.degradeRules.push(ele);
          });
        }
      }
    });
  }

  public createDegradeRule(e) {
    this.isCreModVis = true;
  }

  public refresh(e) {
    this.queryDegradeRules();
  }

  public updateDegradeRule(rule) {
    this.isUpdModVis = true;
    this.currentRule = rule;
  }

  public deleteDegradeRule(rule) {
    this.isDelModVis = true;
    this.currentRule = rule;
  }

  public creModClose(e) {
    this.isCreModVis = e.isVisible;
    if (e.refresh) {
      this.queryDegradeRules();
    }
  }

  public delModClose(e) {
    this.isDelModVis = e.isVisible;
    if (e.refresh) {
      this.queryDegradeRules();
    }
  }

  public updModClose(e) {
    this.isUpdModVis = e.isVisible;
    if (e.refresh) {
      this.queryDegradeRules();
    }
  }
}
