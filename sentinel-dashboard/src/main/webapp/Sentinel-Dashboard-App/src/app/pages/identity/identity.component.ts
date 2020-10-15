import {
  Component, 
  OnInit
} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { KieIdentityService } from 'src/app/services/kie-identity/kie-identity.service';
import { NzButtonSize } from 'ng-zorro-antd/button';
import { KieInfoService } from 'src/app/services/kie-info/kie-info.service';


@Component({
  selector: 'app-identity',
  templateUrl: './identity.component.html',
  styleUrls: ['./identity.component.css']
})
export class IdentityComponent implements OnInit {
  app: string;
  service: string;
  service_id: string;
  resourceInfos: any[];
  currentResource: string;
  btnSize: NzButtonSize = 'large';
  identityfilter: any;

  ip: string;
  port: number;

  // Modal框 显示控制
  isCreFlowModVis: boolean = false;

  constructor(
    private route: ActivatedRoute,
    private kieIdentityService: KieIdentityService,
    private kieInfoService: KieInfoService
  ) { }

  async ngOnInit() {
    this.route.params.subscribe(res => {
      this.app = res.app;
      this.service = res.service;
      this.service_id = res.service_id;
    });

    const instanceInfos: any = await new Promise(resolve => {
      this.kieInfoService.queryInstanceInfos(this.service_id).subscribe(res => {
        if (res.success) {
          resolve(res);
        }
      });
    });
    this.ip = instanceInfos.data[0].ip;
    this.port = instanceInfos.data[0].port;
    const resources: any = await new Promise(resolve => {
      this.kieIdentityService.queryResource(this.ip, this.port).subscribe(res => {
        if (res.success) {
          resolve(res);
        }
      });
    });
    this.resourceInfos = resources.data;
  }

  public queryResource(): void {
    this.kieIdentityService.queryResource(this.ip, this.port).subscribe(res => {
      if (res.success) {
        this.resourceInfos = res.data;
      }
    })
  }

  public createFlowRule(rowData): void {
    this.currentResource = rowData.resource;
    this.isCreFlowModVis = true;
  }

  public creFlowModClose(e): void {
    this.isCreFlowModVis = e.isVisible;
    if (e.refresh) {
      this.queryResource();
    }
  }

  public refresh(e): void {
    this.queryResource();
  }
}