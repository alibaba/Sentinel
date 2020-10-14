import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { KieSystemService } from 'src/app/services/kie-system/kie-system.service';
import { NzButtonSize } from 'ng-zorro-antd/button';

@Component({
  selector: 'app-system',
  templateUrl: './system.component.html',
  styleUrls: ['./system.component.css']
})
export class SystemComponent implements OnInit {
  app: string;
  service: string;
  service_id: string;
  environments: string[];
  currentEnvir: string;
  systemRules: any[] = [];
  currentRule: any;

  btnSize: NzButtonSize = 'large';
  systemfilter: any;

  isCreModVis: boolean = false;
  isDelModVis: boolean = false;
  isUpdModVis: boolean = false;

  constructor(
    private route: ActivatedRoute,
    private kieFlowService: KieSystemService,
  ) { }

  ngOnInit(): void {
    this.route.params.subscribe(res => {
      this.app = res.app;
      this.service = res.service;
      this.service_id = res.service_id;
      this.queryKieSystemRules();
    });
  }

  public queryKieSystemRules(): void {
    this.kieFlowService.queryKieSystemRules(this.service_id).subscribe(res => {
      this.systemRules = [];
      if (res.success) {
        if (res.data && res.data.length) {
          res.data.forEach(ele => {
            this.systemRules.push(ele);
          });
        }
      }
    });
  }

  public createSystemRule(e): void {
    this.isCreModVis = true;
  }

  public updateSystemRule(rule): void {
    this.currentRule = rule;
    this.isUpdModVis = true;
  }

  public deleteSystemRule(rule): void {
    this.currentRule = rule;
    this.isDelModVis = true;
  }

  public refresh(e): void {
    this.queryKieSystemRules();
  }

  public creModClose(e) {
    this.isCreModVis = e.isVisible;
    if (e.refresh) {
      this.queryKieSystemRules();
    }
  }

  public delModClose(e) {
    this.isDelModVis = e.isVisible;
    if (e.refresh) {
      this.queryKieSystemRules();
    }
  }

  public updModClose(e) {
    this.isUpdModVis = e.isVisible;
    if (e.refresh) {
      this.queryKieSystemRules();
    }
  }
}
