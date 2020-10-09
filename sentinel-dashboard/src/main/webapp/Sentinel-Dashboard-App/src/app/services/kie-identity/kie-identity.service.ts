import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class KieIdentityService {
  constructor(
    private http: HttpClient
  ) { }

  public queryResource(ip: string, port: number): Observable<any> {
    return this.http.get(`/resource/machineResource.json?ip=${ip}&port=${port}`);
  }
}
