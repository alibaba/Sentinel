import { Component, Input, OnInit, Output, EventEmitter } from '@angular/core';
import { NzMessageService } from 'ng-zorro-antd/message';
import { KieDegradeService } from 'src/app/services/kie-degrade/kie-degrade.service';

@Component({
  selector: 'app-delete-degrade',
  templateUrl: './delete-degrade.component.html',
  styleUrls: ['./delete-degrade.component.css']
})
export class DeleteDegradeComponent implements OnInit {
  @Input() isVisible: boolean;

  @Input() app: string;

  @Input() service_id: string;

  @Input() currentRule: any;

  @Output() private delModClose = new EventEmitter()
  
  isOkLoading: boolean = false;
  gradeModeMap: any = {};

  constructor(
    private message: NzMessageService,
    private kieDegradeService: KieDegradeService
  ) { }

  ngOnInit(): void {
    this.mapInit();
  }

  private mapInit() {
    this.gradeModeMap = { 
      0: '慢比例调用',    
      1: '异常比例',      
      2: '异常数'         
    };
  }

  public handleOk() {
    this.isOkLoading = true;
    this.kieDegradeService.deleteKieDegradeRule(this.service_id, this.currentRule.ruleId).subscribe(res => {
      if (res.success) {
        this.isOkLoading = false;
        this.isVisible = false;
        this.delModClose.emit({
          isVisible: this.isVisible,
          refresh: true
        });
        this.message.success('删除降级规则成功');
      } else {
        this.isOkLoading = false;
        this.isVisible = false;
        this.delModClose.emit({
          isVisible: this.isVisible,
          refresh: true
        });
        this.message.error('删除降级规则失败 ' + res.msg);
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
