import { TestBed } from '@angular/core/testing';

import { KieIdentityService } from './kie-identity.service';

describe('KieIdentityService', () => {
  let service: KieIdentityService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(KieIdentityService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
