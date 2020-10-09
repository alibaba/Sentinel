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
  listData: any;

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
    this.listData = [
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
            text: 'resource'
        },
        tooltip: {
            trigger: 'axis'
        },
        legend: {
            data: ['通过QPS', '拒绝QPS']
        },
        grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            containLabel: true
        },
        xAxis: {
            type: 'category',
            boundaryGap: false,
            data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
            splitLine: {
                show: false
            },
        },
        yAxis: {
            type: 'value',
            splitLine: {
                show: false
            },
        },
        series: [
            {
                color: 'green',
                symbol: 'none',
                name: '通过QPS',
                type: 'line',
                stack: '总量',
                data: [120, 132, 101, 134, 90, 230, 210]
                // data: [200, 200, 200, 200, 200, 200, 200]
            },
            {
                color: 'blue',
                symbol: 'none',
                name: '拒绝QPS',
                type: 'line',
                stack: '总量',
                data: [220, 182, 191, 234, 290, 330, 310]
            }
        ]
    };  

    console.log('----------');
    console.log(this.chartOption.series[0].data);

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

    // const metricInfos: any = await new Promise(resolve => {
    //     var param = {
    //         serviceId: this.service_id,
    //         ip: this.selectedIns.ip,
    //         port: this.selectedIns.port
    //     };
    //     this.kieMetricService.queryResourceMetric(param).subscribe(res => {
    //         if (res.success) {
    //             resolve(res);
    //         }
    //     });
    // });

    var param = {
        serviceId: this.service_id,
        ip: this.selectedIns.ip,
        port: this.selectedIns.port
    };
    setInterval(() => {
        this.kieMetricService.queryResourceMetric(param).subscribe(res => {
            if (res.success) {
                console.log(res);
            }
        });
    }, 10000);

    // console.log(metricInfos);
    // metricInfos.data;
  }

  public instanceChange(e): void {
      console.log('instanceChange');
      this.chartOption.series[0].data = [200, 200, 200, 200, 200, 200, 200];
      console.log(this.chartOption);
      console.log(this.chartOption.series[0]);
      this.selectedIns = e;
      var param = {
          serviceId: this.service_id,
          ip: this.selectedIns.ip,
          port: this.selectedIns.port
      };
      this.kieMetricService.queryResourceMetric(param).subscribe(res => {
          if (res.success) {
            console.log("res", res);
          }
      });
  }

}
