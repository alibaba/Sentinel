import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class KieMetricService {
  constructor(
    private http: HttpClient
  ) { }

  public queryResourceMetric(param: any): Observable<any> {
    return this.http.get(`/metric/queryTopResourceMetric.json?serverId=${param.serviceId}&ip=${param.ip}&port=${param.port}&pageIndex=1&pageSize=6`);
  }
}
