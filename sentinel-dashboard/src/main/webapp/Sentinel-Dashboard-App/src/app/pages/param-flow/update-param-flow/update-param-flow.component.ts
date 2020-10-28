import {
  Component,
  EventEmitter,
  Input,
  OnInit,
  Output
} from '@angular/core';

import {KieParamFlowService} from "src/app/services/kie-param-flow/kie-param-flow.service";

import {NzMessageService} from "ng-zorro-antd";

import {FormArray, FormBuilder, FormControl, FormGroup, Validators} from "@angular/forms";

@Component({
  selector: 'app-update-param-flow',
  templateUrl: './update-param-flow.component.html',
  styleUrls: ['./update-param-flow.component.css']
})
export class UpdateParamFlowComponent implements OnInit {
  @Input() isVisible: boolean;

  @Input() currentRule: any;

  @Input() service_id: string;

  @Input() app: string;

  @Output() private updModClose = new EventEmitter();

  isOkLoading: boolean = false;
  paramFlowRuleForm: FormGroup;
  newParamFlowItem: FormGroup;
  isCluster: boolean = false;
  advanceOptionFlag: boolean = false;
  classTypeList: string[] = [];
  gradeModeMap: any;

  get paramFlowItemList(): FormArray {
    return this.paramFlowRuleForm.get('paramFlowItemList') as FormArray;
  }

  constructor(
    private formBuilder: FormBuilder,
    private kieParamFlowService: KieParamFlowService,
    private message: NzMessageService
  ) {
  }

  ngOnInit(): void {
    this.listAndMapInit();
    this.formInit();
    this.statInit();
  }

  private listAndMapInit() {
    this.classTypeList = [
      'int',
      'double',
      'java.lang.String',
      'long',
      'float',
      'char',
      'byte'
    ];
    this.gradeModeMap = {
      0: '线程数',
      1: 'QPS'
    };
  }

  private formInit(): void {
    let rule = this.currentRule.rule;
    this.paramFlowRuleForm = this.formBuilder.group({
      resource: [{value: rule.resource, disabled: true}],
      grade: [rule.grade],
      paramIdx: [rule.paramIdx, [Validators.required, Validators.min(0)]],
      durationInSec: [rule.durationInSec, [Validators.required, Validators.min(1)]],
      count: [rule.count, [Validators.required, Validators.min(0)]],
      clusterMode: [rule.clusterMode],
      thresholdType: [rule.clusterConfig.thresholdType.toString()],
      fallbackToLocalWhenFail: [rule.clusterConfig.fallbackToLocalWhenFail],
      advanceOp: [!!rule.paramFlowItemList.length],
      paramFlowItemList: this.formBuilder.array([])
    });

    rule.paramFlowItemList.forEach(paramFlowItem => this.addParamFLowItem(paramFlowItem));

    this.newParamFlowItem = this.formBuilder.group({
      object: [null, [Validators.required]],
      classType: [null, [Validators.required]],
      count: [null, [Validators.required, Validators.min(0)]]
    }, {
      validator: this.paramFlowItemValidator
    });
  }

  private statInit(): void {
    let rule = this.currentRule.rule;
    this.isCluster = rule.clusterMode;
    this.advanceOptionFlag = !!rule.paramFlowItemList.length;
  }

  public handleOk(): void {
    this.isOkLoading = true;
    let params = this.currentRule;

    let paramFlowFormVal = this.paramFlowRuleForm.value;
    // 封装params
    params.app = this.app;
    params.rule.paramIdx = Number(paramFlowFormVal.paramIdx);
    params.rule.grade = Number(paramFlowFormVal.grade);
    params.rule.clusterMode = paramFlowFormVal.clusterMode;
    params.rule.count = Number(paramFlowFormVal.count);
    params.rule.durationInSec = Number(paramFlowFormVal.durationInSec);
    params.rule.paramFlowItemList = paramFlowFormVal.paramFlowItemList;

    if (this.isCluster) {
      params.rule.clusterConfig.thresholdType = Number(paramFlowFormVal.thresholdType);
      params.rule.clusterConfig.fallbackToLocalWhenFail = paramFlowFormVal.fallbackToLocalWhenFail;
    }

    this.kieParamFlowService.updateKieParamFlowRule(this.service_id, params).subscribe(res => {
      if (res.success) {
        this.isOkLoading = false;
        this.isVisible = false;
        this.updModClose.emit({
          isVisible: this.isVisible,
          refresh: true
        });
        this.message.success('编辑热点规则成功');
      } else {
        this.isOkLoading = false;
        this.isVisible = false;
        this.updModClose.emit({
          isVisible: this.isVisible,
          refresh: true
        });
        this.message.error('编辑热点规则失败 ' + res.msg);
      }
    });
  }

  public handleCancel(): void {
    this.isVisible = false;
    this.updModClose.emit({
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

  public addParamFLowItem(paramFlowItem): void {
    this.paramFlowItemList.push(
      this.formBuilder.group({
        object: [paramFlowItem.object, [Validators.required]],
        classType: [paramFlowItem.classType, [Validators.required]],
        count: [paramFlowItem.count, [Validators.required, Validators.min(0)]]
      }, {
        validator: this.paramFlowItemValidator
      })
    );
  }

  public deletePramFlowItem(index): void {
    this.paramFlowItemList.removeAt(index);
  }

  private paramFlowItemValidator(group: FormGroup): any {
    let classType: FormControl = group.get('classType') as FormControl;
    let object: FormControl = group.get('object') as FormControl;
    let valid = false;
    switch (classType.value) {
      case 'java.lang.String':
        valid = true;
        break;
      default:
        valid = /^[+-]?\d+[.]?\d*$/.test(object.value);
        break;
    }
    return valid ? null : {object: false};
  }
}
