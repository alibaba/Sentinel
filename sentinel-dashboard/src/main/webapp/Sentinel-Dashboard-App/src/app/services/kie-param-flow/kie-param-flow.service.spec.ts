import { TestBed } from '@angular/core/testing';

import { KieParamFlowService } from './kie-param-flow.service';

describe('KieParamFlowService', () => {
  let service: KieParamFlowService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(KieParamFlowService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
