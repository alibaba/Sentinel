import { 
  Component, 
  Input, 
  OnInit,
  Output,
  EventEmitter 
} from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { KieDegradeService } from 'src/app/services/kie-degrade/kie-degrade.service';
import { KieIdentityService } from 'src/app/services/kie-identity/kie-identity.service';
import { KieInfoService } from 'src/app/services/kie-info/kie-info.service';
import { NzMessageService } from 'ng-zorro-antd/message';

@Component({
  selector: 'app-create-degrade',
  templateUrl: './create-degrade.component.html',
  styleUrls: ['./create-degrade.component.css']
})
export class CreateDegradeComponent implements OnInit {
  @Input() isVisible: boolean;

  @Input() app: string;

  @Input() service_id: string;

  @Input() currentRule: any;

  @Output() private creModClose = new EventEmitter();

  isOkLoading: boolean = false;
  degradeRuleForm: FormGroup;
  gradeModeMap: any = {}
  gradeMode: string = '0';

  resourceList: string[] = [];
  ip: string;
  port: number;
  autocomOption: any[] = [];

  constructor(
    private kieDegradeService: KieDegradeService,
    private formBuilder: FormBuilder,
    private kieInfoService: KieInfoService,
    private kieIdentityService: KieIdentityService,
    private message: NzMessageService
  ) { }

  ngOnInit(): void {
    this.mapInit();
    this.formInit();
    this.queryResource();
  }

  private mapInit() {
    this.gradeModeMap = { // count对应关系
      0: '慢比例调用',     // 最大 RT
      1: '异常比例',       // 比例阈值
      2: '异常数'          // 异常数
    };
  }

  private formInit() {
    this.degradeRuleForm = this.formBuilder.group({
      resource: [ null, [ Validators.required ]],
      grade: [ '0', []],
      count: [ null, [ Validators.required ]],
      slowRatioThreshold: [ null, []],
      timeWindow: [ null, [ Validators.required ]],
      minRequestAmount: [ 5, [ Validators.required ]]
    });
  }

  private async queryResource() {
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
    resources.data.forEach(ele => {
      this.resourceList.push(ele.resource);
    });
    this.autocomOption = this.resourceList;
  }

  public handleOk() {
    this.isOkLoading = true;
    var value = this.degradeRuleForm.value;
    // 封装param
    var param = {
      app: this.app,
      limitApp: 'default',
      resource: '',
      grade: 0,
      count: 0,
      minRequestAmount: 5,
      statIntervalMs: 1000,
      timeWindow: 0,
      slowRatioThreshold: 1,
    };
    param.resource = value.resource;
    param.grade = Number(value.grade);
    param.count = value.count;
    param.minRequestAmount = value.minRequestAmount;
    param.timeWindow = value.timeWindow;
    param.slowRatioThreshold = value.slowRatioThreshold
    this.kieDegradeService.createKieDegradeRule(this.service_id, param).subscribe(res => {
      if (res.success) {
        this.isOkLoading = false;
        this.isVisible = false;
        this.creModClose.emit({
          isVisible: this.isVisible,
          refresh: true
        });
        this.message.success('新增降级规则成功');
      } else {
        this.isOkLoading = false;
        this.isVisible = false;
        this.creModClose.emit({
          isVisible: this.isVisible,
          refresh: true
        });
        this.message.error('新增降级规则失败 ' + res.msg)
      }
    });
  }

  public handleCancel() {
    this.isVisible = false;
    this.creModClose.emit({
      isVisible: this.isVisible,
      refresh: false
    });
  }

  public onInput(e) {
    const value = (e.target as HTMLInputElement).value;
    this.autocomOption = this.resourceList.includes(value) ? this.resourceList : [value, ...this.resourceList];
  }

  public greadeModeChange(e) {
    this.gradeMode = e;
  }
}
