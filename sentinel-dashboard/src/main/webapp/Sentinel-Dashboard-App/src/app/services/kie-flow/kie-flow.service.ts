import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class KieFlowService {
  constructor(
    private http: HttpClient
  ) { }

  public queryKieFlowRules(service_id: string): Observable<any> {
    return this.http.get(`/kie/flow/rules?serverId=${service_id}`);
  }

  public createKieFlowRule(service_id: string, param: any): Observable<any> {
    return this.http.post(`/kie/flow/${service_id}/rule`, param);
  }

  public deleteKieFlowRule(service_id: string, rule_id: string): Observable<any> {
    return this.http.delete(`/kie/flow/${service_id}/rule/${rule_id}`);
  }

  public updateKieFlowRule(service_id: string, param: any): Observable<any> {
    return this.http.put(`/kie/flow/${service_id}/rule`, param);
  }
}
