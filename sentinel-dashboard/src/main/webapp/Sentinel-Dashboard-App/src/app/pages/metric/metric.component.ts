import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { KieMetricService } from 'src/app/services/kie-metric/kie-metric.service';
import { KieInfoService } from 'src/app/services/kie-info/kie-info.service';


@Component({
  selector: 'app-metric',
  templateUrl: './metric.component.html',
  styleUrls: ['./metric.component.css']
})
export class MetricComponent implements OnInit {
  chartOption: any = {};
  app: string;
  service: string;
  service_id: string;
  data: any;

  // [{ip: ip,port: port},...]
  instanceList: any = [];
  selectedIns: any;

  constructor(
    private route: ActivatedRoute,
    private kieMetricService: KieMetricService,
    private kieInfoService: KieInfoService
  ) { }

  async ngOnInit() {
    this.route.params.subscribe(res => {
    this.app = res.app;
    this.service = res.service;
    this.service_id = res.service_id;
    });
    this.data = [
        {
          title: 'Ant Design Title 1'
        },
        {
          title: 'Ant Design Title 2'
        },
        {
          title: 'Ant Design Title 3'
        },
        {
          title: 'Ant Design Title 5'
        }
    ];

    this.chartOption = {
      title: {
          text: '折线图堆叠'
      },
      tooltip: {
          trigger: 'axis'
      },
      legend: {
          data: ['邮件营销', '联盟广告', '视频广告', '直接访问', '搜索引擎']
      },
      grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
      },
      toolbox: {
          feature: {
              saveAsImage: {}
          }
      },
      xAxis: {
          type: 'category',
          boundaryGap: false,
          data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
      },
      yAxis: {
          type: 'value'
      },
      series: [
          {
              name: '邮件营销',
              type: 'line',
              stack: '总量',
              data: [120, 132, 101, 134, 90, 230, 210]
          },
          {
              name: '联盟广告',
              type: 'line',
              stack: '总量',
              data: [220, 182, 191, 234, 290, 330, 310]
          },
          {
              name: '视频广告',
              type: 'line',
              stack: '总量',
              data: [150, 232, 201, 154, 190, 330, 410]
          },
          {
              name: '直接访问',
              type: 'line',
              stack: '总量',
              data: [320, 332, 301, 334, 390, 330, 320]
          },
          {
              name: '搜索引擎',
              type: 'line',
              stack: '总量',
              data: [820, 932, 901, 934, 1290, 1330, 1320]
          }
      ]
    };  

    const instanceInfos: any = await new Promise(resolve => {
        this.kieInfoService.queryInstanceInfos(this.service_id).subscribe(res => {
            if (res.success) {
                resolve(res);
            }
        });
    });
    instanceInfos.data.map(ele => {
        this.instanceList.push({
            ip: ele.ip,
            port: ele.port
        });
    });
    this.selectedIns = this.instanceList[0];
    this.instanceList.push({
        ip: "1234",
        port: 123
    });

    const metricInfos: any = await new Promise(resolve => {
        var param = {
            serviceId: this.service_id,
            ip: this.selectedIns.ip,
            port: this.selectedIns.port
        };
        this.kieMetricService.queryResourceMetric(param).subscribe(res => {
            if (res.success) {
                resolve(res);
            }
        });
    });
    console.log(metricInfos);

  }

  public instanceChange(e): void {
      console.log('instanceChange');
      console.log(e);
  }

}
