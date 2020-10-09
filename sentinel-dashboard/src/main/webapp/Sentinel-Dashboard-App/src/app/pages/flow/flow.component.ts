import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { KieFlowService } from 'src/app/services/kie-flow/kie-flow.service';
import { NzButtonSize } from 'ng-zorro-antd/button';


interface FlowRule {
  // 表格数据
  resource: string; // 资源名
  limitApp: string; // 来源应用
  strategy: string; // 流控模式 0直接 1关联 2链路
  grade: string; // 阈值类型  0线程数 else QPS
  count: number; // 阈值
  countMode: string; // 阈值模式 '单机' '集群均摊' '集群总体' '集群' 具体逻辑看demo
  controlBehavior: string; //流控效果 0快速失败 1Warm Up 2排队等待 3预热排队 
  // 其余数据
  id: string;
  ruleId: string;
  app: string;
  ip: string;
  port: number;
  refResource: string;
  warmUpPeriodSec: number;
  maxQueueingTimeMs: number;
  clusterMode: boolean;
}

@Component({
  selector: 'app-flow',
  templateUrl: './flow.component.html',
  styleUrls: ['./flow.component.css']
})
export class FlowComponent implements OnInit {
  app: string;
  service: string;
  service_id: string;
  environments: string[];
  currentEnvir: string;
  flowRules: any[] = [];
  strategyModeMap: any;
  controlBehaviorModeMap: any;
  clusterModeMap: any;
  gradeModeMap: any;
  btnSize: NzButtonSize = 'large';
  currentRule: any;

  // Model 对话框显示
  isCreModVis: boolean = false;
  isDelModVis: boolean = false;
  isUpdModVis: boolean = false;

  constructor(
    private route: ActivatedRoute,
    private kieFlowService: KieFlowService,
  ) { }

  ngOnInit(): void {
    // console.log('FlowComponent init!');
    this.mapInit();
    this.route.params.subscribe(res => {
      this.app = res.app;
      this.service = res.service;
      this.service_id = res.service_id;
    });
    this.queryKieFlowRules();
  }

  private mapInit(): void {
    this.strategyModeMap = {
      0: '直接',
      1: '关联',
      2: '链路'
    };
    this.controlBehaviorModeMap = {
      0: '快速失败',
      1: 'Warm up',
      2: '排队等待',
      3: '预热排队'
    };
    this.clusterModeMap = {
      0: '集群均摊',
      1: '集群总体'
    };
    this.gradeModeMap ={
      0: '线程数',
      1: 'QPS'
    }
  }

  public queryKieFlowRules(): void {
    this.kieFlowService.queryKieFlowRules(this.service_id).subscribe(res => {
      this.flowRules = [];
      if (res.success) {
        if (res.data && res.data.length) {
          res.data.forEach(ele => {
            this.flowRules.push(ele);
          });
        }
        // console.log("this.flowRules", this.flowRules);
      }
    });
  }

  public renderCountMode(flowRule): string {
    if (!flowRule.clusterMode) {
      return '单机';
    }
    return this.clusterModeMap[flowRule.clusterConfig.thresholdType] || '集群';
  }

  public createFlowRule(e): void {
    this.isCreModVis = true;
  }

  public updateFlowRule(rule): void {
    this.currentRule = rule;
    this.isUpdModVis = true;
  }

  public deleteFlowRule(rule): void {
    this.currentRule = rule;
    this.isDelModVis = true;
  }

  public refresh(e): void {
    this.queryKieFlowRules();
  }

  public creModClose(e) {
    this.isCreModVis = e.isVisible;
    if (e.refresh) {
      this.queryKieFlowRules();
    }
  }

  public delModClose(e) {
    this.isDelModVis = e.isVisible;
    if (e.refresh) {
      this.queryKieFlowRules();
    }
  }

  public updModClose(e) {
    this.isUpdModVis = e.isVisible;
    if (e.refresh) {
      this.queryKieFlowRules();
    }
  }
}
