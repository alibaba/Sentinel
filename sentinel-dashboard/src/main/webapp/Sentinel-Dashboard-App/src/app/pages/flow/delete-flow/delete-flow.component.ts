import { 
  Component, 
  OnInit, 
  Input,
  Output,
  EventEmitter 
} from '@angular/core';

import { KieFlowService } from 'src/app/services/kie-flow/kie-flow.service';

@Component({
  selector: 'app-delete-flow',
  templateUrl: './delete-flow.component.html',
  styleUrls: ['./delete-flow.component.css']
})
export class DeleteFlowComponent implements OnInit {
  @Input() isVisible: boolean;

  @Input() service_id: string;

  @Input() currentRule: any;

  @Output() private delModClose = new EventEmitter();

  isOkLoading: boolean = false;
  gradeModeMap: any = {};

  constructor(
    private kieFlowService: KieFlowService
  ) { }

  ngOnInit(): void {
    this.mapInit();
    console.log("this.currentRule", this.currentRule);
  }

  private mapInit() {
    this.gradeModeMap = {
      0: '线程数',
      1: 'QPS'
    }
  }

  public handleOk() {
    this.isOkLoading = true;
    this.kieFlowService.deleteKieFlowRule(this.service_id, this.currentRule.ruleId).subscribe(res => {
      if (res.success) {
        this.isOkLoading = false;
        this.isVisible = false;
        this.delModClose.emit({
          isVisible: this.isVisible,
          refresh: true
        });
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
