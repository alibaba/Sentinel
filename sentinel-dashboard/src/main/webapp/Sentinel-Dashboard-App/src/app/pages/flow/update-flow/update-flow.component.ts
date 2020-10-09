import { 
  Component, 
  OnInit, 
  Input,
  Output,
  EventEmitter 
} from '@angular/core';

import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';

import { KieFlowService } from 'src/app/services/kie-flow/kie-flow.service';

@Component({
  selector: 'app-update-flow',
  templateUrl: './update-flow.component.html',
  styleUrls: ['./update-flow.component.css']
})
export class UpdateFlowComponent implements OnInit {
  @Input() isVisible: boolean;

  @Input() currentRule: any;

  @Input() service_id: any;

  @Output() private updModClose = new EventEmitter();
  flowRuleForm: FormGroup;
  isOkLoading: boolean = false;
  strategyModeMap: any = {};
  controlBehaviorModeMap: any = {};
  clusterModeMap: any = {};
  gradeModeMap: any = {};
  isCluster: boolean = false;
  advanceOptionFlag: boolean = false;
  strategyMode: number = 0;
  controlBehaviorMode: number = 0;

  constructor(
    private formBuilder: FormBuilder,
    private kieFlowService: KieFlowService
  ) { }

  ngOnInit(): void {
    this.statInit();
    this.formInit();
    this.mapInit();
  }

  private formInit(): void {
    this.flowRuleForm = this.formBuilder.group({
      resource: new FormControl({
        value: this.currentRule.resource,
        disabled: true
      }, Validators.required),
      limitApp: [ this.currentRule.limitApp, []],
      grade: [ this.currentRule.grade.toString(), []],
      clusterMode: [ this.currentRule.clusterMode, []],
      count: [ this.currentRule.count.toString(), []],
      thresholdType: [ this.currentRule.clusterConfig.thresholdType.toString(), []],
      fallbackToLocalWhenFail: [ this.currentRule.clusterConfig.fallbackToLocalWhenFail, []],
      advanceOp: [ this.advanceOptionFlag, []],
      strategy: [ this.currentRule.strategy.toString(), []],
      refResource: [ this.currentRule.refResource, []],
      controlBehavior: [ this.currentRule.controlBehavior.toString(), []],
      warmUpPeriodSec: [ this.currentRule.warmUpPeriodSec.toString(), []],
      maxQueueingTimeMs: [ this.currentRule.maxQueueingTimeMs.toString(), []]
    })
  }

  private mapInit(): void {
    this.strategyModeMap = {
      0: '直接',
      1: '关联',
      2: '链路'
    };
    this.controlBehaviorModeMap = {
      0: '快速失败',
      1: 'Warm Up',
      2: '排队等待'
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

  private statInit(): void {
    this.isCluster = this.currentRule.clusterMode;
    this.advanceOptionFlag = this.currentRule.strategy !== 0 || this.currentRule.controlBehavior !== 0;
    this.strategyMode = this.currentRule.strategy;
    this.controlBehaviorMode = this.currentRule.controlBehavior;
  }

  public handleOk(): void {
    this.isOkLoading = true;
    var value = this.flowRuleForm.value;

    // 封装param
    var param = this.currentRule;
    param.limitApp = value.limitApp;
    param.grade = Number(value.grade);
    param.clusterMode = value.clusterMode;
    param.count = Number(value.count);
    param.clusterConfig.thresholdType = Number(value.thresholdType);
    param.clusterConfig.fallbackToLocalWhenFail = value.fallbackToLocalWhenFail;
    param.strategy = Number(value.strategy);
    param.refResource = value.refResource;
    param.controlBehavior = Number(value.controlBehavior);
    param.warmUpPeriodSec = Number(value.warmUpPeriodSec);
    param.maxQueueingTimeMs = Number(value.maxQueueingTimeMs);

    this.kieFlowService.updateKieFlowRule(this.service_id, param).subscribe(res => {
      if (res.success) {
        this.isVisible = false;
        this.isOkLoading = false;
        this.updModClose.emit({
          isVisible: this.isVisible,
          refresh: true
        });  
      }
    });
  }

  public handleCancel(): void {
    this.isVisible = false;
    this.updModClose.emit({
      isVisible: this.isVisible,
      refresh: false
    })
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
}
