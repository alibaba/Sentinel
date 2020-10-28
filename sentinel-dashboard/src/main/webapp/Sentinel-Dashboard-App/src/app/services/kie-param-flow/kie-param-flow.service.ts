import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class KieParamFlowService {

  constructor(
    private http: HttpClient
  ) { }

  public queryKieParamFlowRules(service_id: string): Observable<any> {
    return this.http.get(`/kie/paramFlow/rules?serverId=${service_id}`);
  }

  public createKieParamFlowRule(service_id: string, param: any): Observable<any> {
    return this.http.post(`/kie/paramFlow/${service_id}/rule`, param);
  }

  public deleteKieParamFlowRule(service_id: string, rule_id: string): Observable<any> {
    return this.http.delete(`/kie/paramFlow/${service_id}/rule/${rule_id}`);
  }

  public updateKieParamFlowRule(service_id: string, param: any): Observable<any> {
    return this.http.put(`/kie/paramFlow/${service_id}/rule`, param);
  }
}
