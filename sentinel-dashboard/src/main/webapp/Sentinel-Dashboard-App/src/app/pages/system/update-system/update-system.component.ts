import {
  Component,
  OnInit,
  Input,
  Output,
  EventEmitter
} from '@angular/core';

import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';

import { KieSystemService } from 'src/app/services/kie-system/kie-system.service';
import { KieInfoService } from 'src/app/services/kie-info/kie-info.service';
import { NzMessageService } from 'ng-zorro-antd/message';

@Component({
  selector: 'app-update-system',
  templateUrl: './update-system.component.html',
  styleUrls: ['./update-system.component.css']
})
export class UpdateSystemComponent implements OnInit {
  @Input() isVisible: boolean;
  @Input() app: string;
  @Input() service_id: string;
  @Input() currentRule: any;

  @Output() private updModClose = new EventEmitter();

  isOkLoading: boolean = false;
  systemRuleForm: FormGroup;
  sThK: any;

  ip: string;
  port: number;

  constructor(
    private formBuilder: FormBuilder,
    private kieSystemService: KieSystemService,
    private kieInfoService: KieInfoService,
    private message: NzMessageService
  ) { }

  ngOnInit(): void {
    this.formInit();
  }

  private formInit(): void {
    this.systemRuleForm = this.formBuilder.group({
        thresholdKind: [ this.sThK, [ Validators.required ]],
        thresholdValue: [ null, [ 
          Validators.required,
          Validators.min(0)
        ]],
    });
    if (this.currentRule.highestSystemLoad >= 0) {
      this.sThK = 'load';
      this.systemRuleForm.controls.thresholdValue.patchValue(this.currentRule.highestSystemLoad);
    }
    if (this.currentRule.avgRt >= 0) {
      this.sThK = 'rt';
      this.systemRuleForm.controls.thresholdValue.patchValue(this.currentRule.avgRt);
    }
    if (this.currentRule.maxThread >= 0) {
      this.sThK = 'threadn';
      this.systemRuleForm.controls.thresholdValue.patchValue(this.currentRule.maxThread);
    }
    if (this.currentRule.qps >= 0) {
      this.sThK = 'eqps';
      this.systemRuleForm.controls.thresholdValue.patchValue(this.currentRule.qps);
    }
    if (this.currentRule.highestCpuUsage >= 0) {
      this.sThK = 'cpur';
      this.systemRuleForm.controls.thresholdValue.patchValue(this.currentRule.highestCpuUsage);
    }
  }

  public handleOk(): void {
    this.isOkLoading = true;
    var params = {
      app: this.app,
      highestSystemLoad: null, // LOAD
      avgRt: null, // RT
      maxThread: null, // 线程数
      qps: null, // 入口 QPS
      highestCpuUsage: null, // CPU 使用率
      ip: null,
      port: null,
    };
    var thVal = this.systemRuleForm.value.thresholdValue;
    switch (this.systemRuleForm.value.thresholdKind) {
      case 'load' : params.highestSystemLoad = thVal; break;
      case 'rt' : params.avgRt = thVal; break;
      case 'threadn' : params.maxThread = thVal; break;
      case 'eqps' : params.qps = thVal; break;
      case 'cpur' : params.highestCpuUsage = thVal; break;
    }

    this.kieSystemService.updateKieSystemRule(this.service_id, params).subscribe(res => {
      if (res.success) {
        this.isOkLoading = false;
        this.isVisible = false;
        this.updModClose.emit({
          isVisible: this.isVisible,
          refresh: true
        });
        this.message.success('编辑系统保护规则成功');
      } else {
        this.isOkLoading = false;
        this.isVisible = false;
        this.updModClose.emit({
          isVisible: this.isVisible,
          refresh: true
        });
        this.message.error('编辑系统保护规则失败 ' + res.msg);
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

}
