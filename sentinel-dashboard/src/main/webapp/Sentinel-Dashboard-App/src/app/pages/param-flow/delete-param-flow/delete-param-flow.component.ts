import {
  Component,
  EventEmitter,
  Input,
  OnInit,
  Output
} from '@angular/core';

import {KieParamFlowService} from "src/app/services/kie-param-flow/kie-param-flow.service";

import {NzMessageService} from "ng-zorro-antd";

@Component({
  selector: 'app-delete-param-flow',
  templateUrl: './delete-param-flow.component.html',
  styleUrls: ['./delete-param-flow.component.css']
})
export class DeleteParamFlowComponent implements OnInit {
  @Input() isVisible: boolean;

  @Input() service_id: string;

  @Input() currentRule: any;

  @Output() private delModClose = new EventEmitter();

  isOkLoading: boolean = false;
  gradeModeMap: any;

  constructor(
    private kieParamFlowService: KieParamFlowService,
    private message: NzMessageService
  ) { }

  ngOnInit(): void {
    this.mapInit();
  }

  private mapInit() {
    this.gradeModeMap = {
      0: '线程数',
      1: 'QPS'
    }
  }

  public handleOk() {
    this.isOkLoading = true;
    this.kieParamFlowService.deleteKieParamFlowRule(this.service_id, this.currentRule.ruleId).subscribe(res => {
      if (res.success) {
        this.isOkLoading = false;
        this.isVisible = false;
        this.delModClose.emit({
          isVisible: this.isVisible,
          refresh: true
        });
        this.message.success('删除热点规则成功');
      } else {
        this.isOkLoading = false;
        this.isVisible = false;
        this.delModClose.emit({
          isVisible: this.isVisible,
          refresh: true
        });
        this.message.error('删除热点规则失败 ' + res.msg);
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
