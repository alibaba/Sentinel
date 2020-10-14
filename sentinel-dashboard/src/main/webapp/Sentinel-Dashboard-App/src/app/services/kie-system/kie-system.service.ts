import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class KieSystemService {

  constructor(
    private http: HttpClient
  ) { }

  public queryKieSystemRules(service_id: string): Observable<any> {
    return this.http.get(`/kie/system/rules?serverId=${service_id}`);
  }

  public createKieSystemRule(service_id: string, param: any): Observable<any> {
    return this.http.post(`/kie/system/${service_id}/rule`, param);
  }

  public deleteKieSystemRule(service_id: string, rule_id: string): Observable<any> {
    return this.http.delete(`/kie/system/${service_id}/rule/${rule_id}`);
  }

  public updateKieSystemRule(service_id: string, param: any): Observable<any> {
    return this.http.put(`/kie/system/${service_id}/rule`, param);
  }
}
