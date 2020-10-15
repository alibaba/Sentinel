import { 
  Component, 
  OnInit, 
  Input,
  Output,
  EventEmitter 
} from '@angular/core';

import { KieSystemService } from 'src/app/services/kie-system/kie-system.service';
import { NzMessageService } from 'ng-zorro-antd/message';

@Component({
  selector: 'app-delete-system',
  templateUrl: './delete-system.component.html',
  styleUrls: ['./delete-system.component.css']
})
export class DeleteSystemComponent implements OnInit {
  @Input() isVisible: boolean;

  @Input() service_id: string;

  @Input() currentRule: any;

  @Output() private delModClose = new EventEmitter();

  isOkLoading: boolean = false;
  currentThresholdKind: any;
  currentThresholdValue: any;

  constructor(
    private kieSystemService: KieSystemService,
    private message: NzMessageService
  ) { }

  ngOnInit(): void {
    this.initCurrentData();
  }

  initCurrentData(): void {
    if (this.currentRule.highestSystemLoad >= 0) {
      this.currentThresholdKind = '系统 load';
      this.currentThresholdValue = this.currentRule.highestSystemLoad;
    }
    if (this.currentRule.avgRt >= 0) {
      this.currentThresholdKind = '平均 RT';
      this.currentThresholdValue = this.currentRule.avgRt;
    }
    if (this.currentRule.maxThread >= 0) {
      this.currentThresholdKind = '并发数';
      this.currentThresholdValue = this.currentRule.maxThread;
    }
    if (this.currentRule.qps >= 0) {
      this.currentThresholdKind = '入口 QPS';
      this.currentThresholdValue = this.currentRule.qps;
    }
    if (this.currentRule.highestCpuUsage >= 0) {
      this.currentThresholdKind = 'CPU 使用率';
      this.currentThresholdValue = this.currentRule.highestCpuUsage;
    }
  }

  public handleOk() {
    this.isOkLoading = true;
    this.kieSystemService.deleteKieSystemRule(this.service_id, this.currentRule.ruleId).subscribe(res => {
      if (res.success) {
        this.isOkLoading = false;
        this.isVisible = false;
        this.delModClose.emit({
          isVisible: this.isVisible,
          refresh: true
        });
        this.message.success('删除系统保护规则成功');
      } else {
        this.isOkLoading = false;
        this.isVisible = false;
        this.delModClose.emit({
          isVisible: this.isVisible,
          refresh: true
        });
        this.message.error('删除系统保护规则失败 ' + res.msg);
      }
    });
  }

  public handleCancel() {
    this.isVisible = false;
    this.delModClose.emit({
      isVisible: this.isVisible,
      refresh: false
    });
  }

}
