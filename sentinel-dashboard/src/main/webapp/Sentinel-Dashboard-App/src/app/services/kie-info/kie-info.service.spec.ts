import { TestBed } from '@angular/core/testing';

import { KieInfoService } from './kie-info.service';

describe('KieInfoService', () => {
  let service: KieInfoService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(KieInfoService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
