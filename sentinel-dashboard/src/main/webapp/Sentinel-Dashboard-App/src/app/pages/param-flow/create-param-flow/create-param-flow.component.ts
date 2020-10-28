import {
  Component,
  EventEmitter,
  Input,
  OnInit,
  Output
} from '@angular/core';

import {KieParamFlowService} from "src/app/services/kie-param-flow/kie-param-flow.service";
import {KieIdentityService} from "src/app/services/kie-identity/kie-identity.service";
import {KieInfoService} from "src/app/services/kie-info/kie-info.service";

import {NzMessageService} from "ng-zorro-antd";

import {FormArray, FormBuilder, FormControl, FormGroup, Validators} from "@angular/forms";

@Component({
  selector: 'app-create-param-flow',
  templateUrl: './create-param-flow.component.html',
  styleUrls: ['./create-param-flow.component.css']
})
export class CreateParamFlowComponent implements OnInit {
  @Input() isVisible: boolean;

  @Input() app: string;

  @Input() service_id: string;

  @Output() private creModClose = new EventEmitter();

  isOkLoading: boolean = false;
  paramFlowRuleForm: FormGroup;
  newParamFlowItem: FormGroup;
  isCluster: boolean = false;
  advanceOptionFlag: boolean = false;
  ip: string;
  port: number;
  classTypeList: string[] = [];
  gradeModeMap: any;
  resourceList: string[] = [];
  autoComOption: any[] = [];

  get paramFlowItemList(): FormArray {
    return this.paramFlowRuleForm.get('paramFlowItemList') as FormArray;
  }

  constructor(
    private formBuilder: FormBuilder,
    private kieParamFlowService: KieParamFlowService,
    private kieIdentityService: KieIdentityService,
    private kieInfoService: KieInfoService,
    private message: NzMessageService
  ) {
  }

  ngOnInit(): void {
    this.listAndMapInit();
    this.formInit();
    this.queryResource();
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
    this.paramFlowRuleForm = this.formBuilder.group({
      resource: [null, [Validators.required]],
      grade: ['1', []],
      paramIdx: [null, [Validators.required, Validators.min(0)]],
      durationInSec: [1, [Validators.required, Validators.min(1)]],
      count: [null, [Validators.required, Validators.min(0)]],
      clusterMode: [false],
      thresholdType: ['0', []],
      fallbackToLocalWhenFail: [false, []],
      advanceOp: [false, []],
      paramFlowItemList: this.formBuilder.array([]),
    });

    this.newParamFlowItem = this.formBuilder.group({
      object: [null, [Validators.required]],
      classType: [null, [Validators.required]],
      count: [null, [Validators.required, Validators.min(0)]]
    }, {
      validator: this.paramFlowItemValidator
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
    this.autoComOption = this.resourceList;
  }

  public onInput(e) {
    const value = (e.target as HTMLInputElement).value;
    this.autoComOption = this.resourceList.includes(value) ? this.resourceList : [value, ...this.resourceList];
  }

  public handleOk(): void {
    this.isOkLoading = true;
    let params = {
      app: this.app,
      ip: null,
      port: null,
      rule: {
        grade: 1,
        burstCount: 0,
        controlBehavior: 0,
        limitApp: 'default',
        clusterMode: false,
        clusterConfig: {
          thresholdType: 0,
          fallbackToLocalWhenFail: false
        },
        resource: '',
        count: 0,
        durationInSec: 1,
        maxQueueingTimeMs: 0,
        paramIdx: null,
        paramFlowItemList: []
      }
    };

    let paramFlowFormVal = this.paramFlowRuleForm.value;
    // 封装params
    params.rule.resource = paramFlowFormVal.resource;
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

    this.kieParamFlowService.createKieParamFlowRule(this.service_id, params).subscribe(res => {
      if (res.success) {
        this.isOkLoading = false;
        this.isVisible = false;
        this.creModClose.emit({
          isVisible: this.isVisible,
          refresh: true
        });
        this.message.success('新增热点规则成功');
      } else {
        this.isOkLoading = false;
        this.isVisible = false;
        this.creModClose.emit({
          isVisible: this.isVisible,
          refresh: true
        });
        this.message.error('新增热点规则失败 ' + res.msg);
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

  public addParamFLowItem(newParamFlowItem): void {
    this.paramFlowItemList.push(
      this.formBuilder.group({
        object: [newParamFlowItem.object, [Validators.required]],
        count: [newParamFlowItem.count, [Validators.required, Validators.min(0)]],
        classType: [newParamFlowItem.classType]
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
