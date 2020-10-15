import { TestBed } from '@angular/core/testing';

import { KieDegradeService } from './kie-degrade.service';

describe('KieDegradeService', () => {
  let service: KieDegradeService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(KieDegradeService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
