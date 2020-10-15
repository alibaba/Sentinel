import { TestBed } from '@angular/core/testing';

import { KieSystemService } from './kie-system.service';

describe('KieSystemService', () => {
  let service: KieSystemService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(KieSystemService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
