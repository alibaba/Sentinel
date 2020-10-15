import { 
  Component, 
  OnInit, 
  Input,
  Output,
  EventEmitter 
} from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

import { KieFlowService } from 'src/app/services/kie-flow/kie-flow.service';
import { KieIdentityService } from 'src/app/services/kie-identity/kie-identity.service';
import { KieInfoService } from 'src/app/services/kie-info/kie-info.service';
import { NzMessageService } from 'ng-zorro-antd/message';

interface FlowRule {
  // 表格数据
  resource: string; // 资源名
  limitApp: string; // 来源应用
  strategy: string; // 流控模式 0直接 1关联 2链路
  grade: string; // 阈值类型  0线程数 1QPS
  count: number; // 阈值
  countMode: string; // 阈值模式 '单机' '集群均摊' '集群总体' '集群' 具体逻辑看demo
  controlBehavior: string; //流控效果 0快速失败 1Warm Up 2排队等待
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
  selector: 'app-create-flow',
  templateUrl: './create-flow.component.html',
  styleUrls: ['./create-flow.component.css']
})
export class CreateFlowComponent implements OnInit {
  @Input() isVisible: boolean;

  @Input() app: string;

  @Input() service_id: string;

  @Output() private creModClose = new EventEmitter();

  isOkLoading: boolean = false;
  flowRuleForm: FormGroup;
  isCluster: boolean = false;
  advanceOptionFlag: boolean = false;
  strategyMode: number = 0;
  strategyModeMap: any = {};
  controlBehaviorMode: number = 0;
  controlBehaviorModeMap: any = {};
  resourceList: string[] = [];
  ip: string;
  port: number;
  autocomOption: any[] = []

  constructor(
    private formBuilder: FormBuilder,
    private kieFlowService: KieFlowService,
    private kieIdentityService: KieIdentityService,
    private kieInfoService: KieInfoService,
    private message: NzMessageService
  ) { }

  ngOnInit(): void {
    this.mapInit();
    this.formInit();
    this.queryResource();
  }

  private mapInit() {
    this.strategyModeMap = {
      0: '直接',
      1: '关联',
      2: '链路'
    };
    this.controlBehaviorModeMap = {
      0: '快速失败',
      1: 'Warm Up',
      2: '排队等待'
    }
  }

  private formInit(): void {
    this.flowRuleForm = this.formBuilder.group({
      resource: [ null, [ Validators.required ]],
      limitApp: [ 'default', [ Validators.required ]],
      grade: [ '1', []],
      clusterMode: [ false ],
      count: [ null, [ Validators.required ]],
      thresholdType: [ '0', []],
      fallbackToLocalWhenFail: [ false, []],
      advanceOp: [ false, []],
      strategy: [ '0', []],
      controlBehavior: [ '0', []],
      refResource: [ null ,[]],
      warmUpPeriodSec: [ null, []],
      maxQueueingTimeMs: [ null, []]
    });
  }

  public async queryResource() {
    const instances: any = await new Promise(resolve => {
      this.kieInfoService.queryInstanceInfos(this.service_id).subscribe(res => {
        if (res.success) {
          resolve(res);
        }
      });
    });
    this.ip = instances.data[0].ip;
    this.port = instances.data[0].port;

    const resources: any = await new Promise(resolve => {
      this.kieIdentityService.queryResource(this.ip, this.port).subscribe(res => {
        if (res.success) {
          resolve(res);
        }
      });
    });
    resources.data.map(ele => {
      this.resourceList.push(ele.resource);
    });
    this.autocomOption = this.resourceList;
  }

  public handleOk(): void {
    this.isOkLoading = true;
    var params = {
      grade: 1,
      strategy: 0,
      controlBehavior: 0,
      app: this.app,
      ip: null,
      port: null,
      limitApp: 'default',
      clusterMode: false,
      clusterConfig: {
        thresholdType: 0,
        fallbackToLocalWhenFail: false
      },
      resource: '',
      count: 0,
      refResource: '',
      warmUpPeriodSec: 0,
      maxQueueingTimeMs: 0
    };

    // 封装params
    params.resource = this.flowRuleForm.value.resource;
    params.limitApp = this.flowRuleForm.value.limitApp;
    params.grade = Number(this.flowRuleForm.value.grade);
    params.clusterMode = this.flowRuleForm.value.clusterMode;
    params.count = Number(this.flowRuleForm.value.count);
    if (this.isCluster) {
      params.clusterConfig.thresholdType = Number(this.flowRuleForm.value.thresholdType);
      params.clusterConfig.fallbackToLocalWhenFail = this.flowRuleForm.value.fallbackToLocalWhenFail;
    } else {
      delete params.clusterConfig.fallbackToLocalWhenFail;
      if (this.advanceOptionFlag) {
        switch (this.strategyModeMap[this.strategyMode]) {
          default:
            break;
          case '直接':
            delete params.refResource;
            break;
          case '关联':
            params.refResource = this.flowRuleForm.value.refResource;
            break;
          case '链路':
            params.refResource = this.flowRuleForm.value.refResource;
            break;
        }
        switch (this.controlBehaviorModeMap[this.controlBehaviorMode]) {
          default:
            break;
          case '快速失败':
            delete params.warmUpPeriodSec;
            delete params.maxQueueingTimeMs;
            break;
          case 'Warm Up':
            params.warmUpPeriodSec = this.flowRuleForm.value.warmUpPeriodSec;
            delete params.maxQueueingTimeMs;
            break;
          case '排队等待':
            params.maxQueueingTimeMs = this.flowRuleForm.value.maxQueueingTimeMs;
            delete params.warmUpPeriodSec;
            break;
        }
      } else {
        delete params.refResource;
        delete params.warmUpPeriodSec;
        delete params.maxQueueingTimeMs;
      }
    }
    this.kieFlowService.createKieFlowRule(this.service_id, params).subscribe(res => {
      if (res.success) {
        this.isOkLoading = false;
        this.isVisible = false;
        this.creModClose.emit({
          isVisible: this.isVisible,
          refresh: true
        });
        this.message.success('新增流控规则成功');
      } else {
        this.isOkLoading = false;
        this.isVisible = false;
        this.creModClose.emit({
          isVisible: this.isVisible,
          refresh: true
        });
        this.message.error('新增流控规则失败 ' + res.msg);
      }
    });
  }

  public handleCancel(): void {
    this.isVisible = false;
    this.creModClose.emit({
      isVisible: this.isVisible,
      refresh: false
    });
  }

  public isClusterSwitch(e): void {
    this.isCluster = e;
  }

  public advanceOpChange(e): void {
    this.advanceOptionFlag = e;
  }

  public strategyModeChange(e): void {
    this.strategyMode = Number(e);

  }

  public controlBehaviorModeChange(e): void {
    this.controlBehaviorMode = Number(e);
  }

  public onInput(e) {
    const value = (e.target as HTMLInputElement).value;
    this.autocomOption = this.resourceList.includes(value) ? this.resourceList : [value, ...this.resourceList];
  }
}
