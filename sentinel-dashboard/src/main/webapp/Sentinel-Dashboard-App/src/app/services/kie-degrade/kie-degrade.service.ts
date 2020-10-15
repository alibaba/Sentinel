import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class KieDegradeService {
  constructor(
    private http: HttpClient
  ) { }

  public queryKieDegradeRules(service_id: string): Observable<any> {
    return this.http.get(`/kie/degrade/rules?serverId=${service_id}`);
  }

  public createKieDegradeRule(service_id: string, param: any): Observable<any> {
    return this.http.post(`/kie/degrade/${service_id}/rule`, param);
  }

  public deleteKieDegradeRule(service_id: string, rule_id: any): Observable<any> {
    return this.http.delete(`/kie/degrade/${service_id}/rule/${rule_id}`)
  }

  public updateKieDegradeRule(service_id: string, param: any): Observable<any> {
    return this.http.put(`/kie/degrade/${service_id}/rule`, param);
  }
}
