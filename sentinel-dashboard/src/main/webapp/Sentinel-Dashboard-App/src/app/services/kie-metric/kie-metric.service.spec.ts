import { TestBed } from '@angular/core/testing';

import { KieMetricService } from './kie-metric.service';

describe('KieMetricService', () => {
  let service: KieMetricService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(KieMetricService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
