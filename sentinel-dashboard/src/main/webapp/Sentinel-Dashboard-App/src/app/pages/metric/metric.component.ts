import { Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { KieMetricService } from 'src/app/services/kie-metric/kie-metric.service';
import { KieInfoService } from 'src/app/services/kie-info/kie-info.service';

@Component({
  selector: 'app-metric',
  templateUrl: './metric.component.html',
  styleUrls: ['./metric.component.css']
})
export class MetricComponent implements OnInit, OnDestroy {
  chartOptionMap: any = {};
  chartOptionTemp: any = {
    title: {
        text: ''
    },
    tooltip: {
        trigger: 'axis'
    },
    legend: {
        data: ['通过QPS', '拒绝QPS']
    },
    xAxis: { 
        type: 'category',
        data: [],
        axisLabel: {
            formatter: (value) => {
                value = value.substr(0,5);
                return value;
            },
            interval: 60
        }
    },
    yAxis: {
        splitLine: {
            show: true,
            lineStyle: {
                type: 'dashed'
            }
        }
    },
    series: [
        {
            color: 'green',
            symbol: 'none',
            name: '通过QPS',
            type: 'line',
            data: []
        }, 
        {
            color: 'blue',
            symbol: 'none',
            name: '拒绝QPS',
            type: 'line',
            data: []
        }
    ]
  };
  app: string;
  service: string;
  service_id: string;
  listData: any[] = [];
  intervalId: any;

  // [{ip: ip,port: port},...]
  instanceList: any = [];
  selectedIns: any;
  INTERVAL_TIME: number = 5000;

  tableDataMap: any = {};

  constructor(
    private route: ActivatedRoute,
    private kieMetricService: KieMetricService,
    private kieInfoService: KieInfoService
  ) { }

  async ngOnInit() {  
    this.listData = [];
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
    instanceInfos.data.map(ele => {
        this.instanceList.push({
            ip: ele.ip,
            port: ele.port
        });
    });
    this.selectedIns = this.instanceList[0];

    var param = {
        serviceId: this.service_id,
        ip: this.selectedIns.ip,
        port: this.selectedIns.port
    };
    var times = 0;
    var tempInterval = setInterval(() => {
        times++;
        if (times === 3) {
            clearInterval(tempInterval);
        }
        this.kieMetricService.queryResourceMetric(param).subscribe(res => {
            if (res.success) {
                for (var key in res.data.metric) {
                    if (!this.listData.includes(key)) {
                        this.listData.push(key);
                        this.chartOptionMap[key] = this.chartOptionTemp;
                    }
                    this.chartOptionMap[key].title.text = key;
                    this.chartOptionMap[key].xAxis.data = [];
                    this.chartOptionMap[key].series[0].data = [];
                    this.chartOptionMap[key].series[1].data = [];
                    res.data.metric[key].forEach(ele => {
                        this.chartOptionMap[key].xAxis.data.push(new Date(ele.timestamp).toString().slice(16, 25));
                        this.chartOptionMap[key].series[0].data.push(ele.passQps);
                        this.chartOptionMap[key].series[1].data.push(ele.blockQps);
                    });
                    this.tableDataMap[key] = new Array();
                    for (var i = 0;i < 5;i++) {
                        if (res.data.metric[key].length) {
                            this.tableDataMap[key].push(res.data.metric[key].pop());
                        }
                    }
                    let mycharts = echarts.init(<HTMLDivElement>document.getElementById(key));
                    mycharts.setOption(this.chartOptionMap[key]);
                }
            }
        });
    }, 100);
    this.intervalId = setInterval(() => {
        this.kieMetricService.queryResourceMetric(param).subscribe(res => {
            if (res.success) {
                for (var key in res.data.metric) {    
                    if (!this.listData.includes(key)) {
                        this.listData.push(key);
                        this.chartOptionMap[key] = this.chartOptionTemp;
                    }
                    this.chartOptionMap[key].title.text = key;
                    this.chartOptionMap[key].xAxis.data = [];
                    this.chartOptionMap[key].series[0].data = [];
                    this.chartOptionMap[key].series[1].data = [];
                    res.data.metric[key].forEach(ele => {
                        this.chartOptionMap[key].xAxis.data.push(new Date(ele.timestamp).toString().slice(16, 25));
                        this.chartOptionMap[key].series[0].data.push(ele.passQps);
                        this.chartOptionMap[key].series[1].data.push(ele.blockQps);
                    });
                    this.tableDataMap[key] = new Array();
                    for (var i = 0;i < 5;i++) {
                        if (res.data.metric[key].length) {
                            this.tableDataMap[key].push(res.data.metric[key].pop());
                        }
                    }
                    let mycharts = echarts.init(<HTMLDivElement>document.getElementById(key));
                    mycharts.setOption(this.chartOptionMap[key]);
                }
            }
        });
    }, this.INTERVAL_TIME);
  }

  ngOnDestroy() {
    clearInterval(this.intervalId);
  }


  public instanceChange(e): void {
    // 清缓存数据
    clearInterval(this.intervalId);
    this.chartOptionMap = {};
    this.listData = [];

    this.selectedIns = e;
    var param = {
        serviceId: this.service_id,
        ip: this.selectedIns.ip,
        port: this.selectedIns.port
    };
    var times = 0;
    var tempInterval = setInterval(() => {
        times++;
        if (times === 3) {
            clearInterval(tempInterval);
        }
        this.kieMetricService.queryResourceMetric(param).subscribe(res => {
            if (res.success) {
                for (var key in res.data.metric) {
                    if (!this.listData.includes(key)) {
                        this.listData.push(key);
                        this.chartOptionMap[key] = this.chartOptionTemp;
                    }
                    this.chartOptionMap[key].title.text = key;
                    this.chartOptionMap[key].xAxis.data = [];
                    this.chartOptionMap[key].series[0].data = [];
                    this.chartOptionMap[key].series[1].data = [];
                    res.data.metric[key].forEach(ele => {
                        this.chartOptionMap[key].xAxis.data.push(new Date(ele.timestamp).toString().slice(16, 25));
                        this.chartOptionMap[key].series[0].data.push(ele.passQps);
                        this.chartOptionMap[key].series[1].data.push(ele.blockQps);
                    });
                    this.tableDataMap[key] = new Array();
                    for (var i = 0;i < 5;i++) {
                        if (res.data.metric[key].length) {
                            this.tableDataMap[key].push(res.data.metric[key].pop());
                        }
                    }
                    let mycharts = echarts.init(<HTMLDivElement>document.getElementById(key));
                    mycharts.setOption(this.chartOptionMap[key]);
                }
            }
        });
    }, 100);
    this.intervalId = setInterval(() => {
    this.kieMetricService.queryResourceMetric(param).subscribe(res => {
        if (res.success) {
            for (var key in res.data.metric) {
                if (!this.listData.includes(key)) {
                    this.listData.push(key);
                    this.chartOptionMap[key] = this.chartOptionTemp;
                }
                this.chartOptionMap[key].title.text = key;
                this.chartOptionMap[key].xAxis.data = [];
                this.chartOptionMap[key].series[0].data = [];
                this.chartOptionMap[key].series[1].data = [];
                res.data.metric[key].forEach(ele => {
                    this.chartOptionMap[key].xAxis.data.push(new Date(ele.timestamp).toString().slice(16, 25));
                    this.chartOptionMap[key].series[0].data.push(ele.passQps);
                    this.chartOptionMap[key].series[1].data.push(ele.blockQps);
                });
                let mycharts = echarts.init(<HTMLDivElement>document.getElementById(key));
                mycharts.setOption(this.chartOptionMap[key]);
            }
        }
    });
    }, this.INTERVAL_TIME);
  }

  public timeFormatter(timestamp: string): string {
    return new Date(timestamp).toString().slice(16, 24);
  }

}
