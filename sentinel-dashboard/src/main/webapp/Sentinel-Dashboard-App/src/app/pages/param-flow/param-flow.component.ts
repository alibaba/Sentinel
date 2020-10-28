import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {KieParamFlowService} from "src/app/services/kie-param-flow/kie-param-flow.service";
import {NzButtonSize} from "ng-zorro-antd";

interface ParamFlowItem{
  object: string;
  count: number;
  classType: string;
}

interface Rule {
  // 表格数据
  resource: string; // 资源名
  paramIdx: number; //参数索引
  grade: string; // 流控模式  0线程数 else QPS
  count: number; // 阈值
  clusterMode: boolean; //是否集群
  paramFlowItemList: ParamFlowItem[]; //例外项参数
  // 其余数据
  burstCount: string;
  controlBehavior: string; //流控效果 0快速失败 1Warm Up 2排队等待 3预热排队
  limitApp: string; // 来源应用
  maxQueueingTimeMs: number;
}

interface ParamFlowRule {
  rule: Rule;
  id: string;
  ruleId: string;
  app: string;
  ip: string;
  port: number;
}

@Component({
  selector: 'app-param-flow',
  templateUrl: './param-flow.component.html',
  styleUrls: ['./param-flow.component.css']
})
export class ParamFlowComponent implements OnInit {
  app: string;
  service: string;
  service_id: string;
  paramFlowRules: any[] = [];
  btnSize: NzButtonSize = 'large';
  gradeModeMap: any;
  currentRule: any;
  paramFlowFilter: any;

  // Model 对话框显示
  isCreModVis: boolean = false;
  isDelModVis: boolean = false;
  isUpdModVis: boolean = false;

  constructor(
    private route: ActivatedRoute,
    private kieParamFlowService: KieParamFlowService,
  ) {
  }

  ngOnInit(): void {
    this.mapInit();
    this.route.params.subscribe(res => {
      this.app = res.app;
      this.service = res.service;
      this.service_id = res.service_id;
    });
    this.queryKieParamFlowRules();
  }

  private mapInit(): void {
    this.gradeModeMap = {
      0: '线程数',
      1: 'QPS'
    }
  }

  public queryKieParamFlowRules(): void {
    this.kieParamFlowService.queryKieParamFlowRules(this.service_id).subscribe(res => {
      this.paramFlowRules = [];
      if (res.success) {
        if (res.data && res.data.length) {
          res.data.forEach(ele => {
            ele.resource = ele.rule.resource;
            this.paramFlowRules.push(ele);
          });
        }
      }
      console.log(this.paramFlowRules);
    });
  }

  public createParamFlowRule(e): void {
    this.isCreModVis = true;
  }

  public updateParamFlowRule(rule): void {
    this.currentRule = rule;
    this.isUpdModVis = true;
  }

  public deleteParamFlowRule(rule): void {
    this.currentRule = rule;
    this.isDelModVis = true;
  }

  public refresh(e): void {
    this.queryKieParamFlowRules();
  }

  public creModClose(e) {
    this.isCreModVis = e.isVisible;
    if (e.refresh) {
      this.queryKieParamFlowRules();
    }
  }

  public updModClose(e) {
    this.isUpdModVis = e.isVisible;
    if (e.refresh) {
      this.queryKieParamFlowRules();
    }
  }

  public delModClose(e) {
    this.isDelModVis = e.isVisible;
    if (e.refresh) {
      this.queryKieParamFlowRules();
    }
  }
}
