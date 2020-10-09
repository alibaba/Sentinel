import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class KieInfoService {
  constructor(
    private http: HttpClient
  ) { }

  public getProjects(): Observable<any> {
    return this.http.get(`/kie/projects`);
  }

  public getKieInfos(project: string, environment: string): Observable<any> {
    return this.http.get(`/kie/kieInfos?project=${project}&environment=${environment}`);
  }

  public queryInstanceInfos(service_id: string): Observable<any> {
    return this.http.get(`/kie/${service_id}/machineInfos`);
  }
}
