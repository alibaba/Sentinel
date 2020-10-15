import { 
  Component, 
  Input, 
  OnInit,
  Output,
  EventEmitter
} from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { NzMessageService } from 'ng-zorro-antd/message';
import { KieDegradeService } from 'src/app/services/kie-degrade/kie-degrade.service';

@Component({
  selector: 'app-update-degrade',
  templateUrl: './update-degrade.component.html',
  styleUrls: ['./update-degrade.component.css']
})
export class UpdateDegradeComponent implements OnInit {
  @Input() isVisible: boolean;

  @Input() app: string;

  @Input() service_id: string;

  @Input() currentRule: any;

  @Output() private updModClose = new EventEmitter();

  degradeRuleForm: FormGroup;
  isOkLoading: boolean = false;
  gradeModeMap: any = {};
  gradeMode: string;

  constructor(
    private formBuilder: FormBuilder,
    private message: NzMessageService,
    private kieDegradeService: KieDegradeService
  ) { }

  ngOnInit(): void {
    this.mapInit();
    this.formInit();
    this.statInit();
  }

  private mapInit() {
    this.gradeModeMap = {
      0: '慢比例调用',
      1: '异常比例',
      2: '异常数'
    };
  }

  private formInit() {
    this.degradeRuleForm = this.formBuilder.group({
      resource: new FormControl({
        value: this.currentRule.resource,
        disabled: true
      }, Validators.required),
      grade: [ this.currentRule.grade.toString(), []],
      count: [ this.currentRule.count, [ Validators.required ]],
      slowRatioThreshold: [ this.currentRule.slowRatioThreshold, []],
      timeWindow: [ this.currentRule.timeWindow, [ Validators.required ]],
      minRequestAmount: [ this.currentRule.minRequestAmount, [ Validators.required ]]
    });
  }

  private statInit() {
    this.gradeMode = this.currentRule.grade;
  }

  public handleOk() {
    this.isOkLoading = true;
    var param = this.currentRule;
    var value = this.degradeRuleForm.value;
    
    // 封装param
    param.grade = Number(value.grade);
    param.count = value.count;
    param.slowRatioThreshold = value.slowRatioThreshold;
    param.timeWindow = value.timeWindow;
    param.minRequestAmount = value.minRequestAmount;
    param.app = this.app;
  
    this.kieDegradeService.updateKieDegradeRule(this.service_id, param).subscribe(res => {
      if (res.success) {
        this.isOkLoading = false;
        this.isVisible = false;
        this.updModClose.emit({
          isVisible: this.isVisible,
          refresh: true
        });
        this.message.success('编辑降级规则成功');  
      } else {
        this.isOkLoading = false;
        this.isVisible = false;
        this.updModClose.emit({
          isVisible: this.isVisible,
          refresh: true
        });
        this.message.error('编辑降级规则失败 ' + res.msg);
      }
    })
    setTimeout(() => {
      
    }, 2000);
  }

  public handleCancel() {
    this.isVisible = false;
    this.updModClose.emit({
      isVisible: this.isVisible,
      refresh: false
    });
  }

  public greadeModeChange(e) {
    this.gradeMode = e;
  }

}
