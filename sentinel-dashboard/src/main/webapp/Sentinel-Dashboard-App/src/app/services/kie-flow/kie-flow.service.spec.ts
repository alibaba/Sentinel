import { TestBed } from '@angular/core/testing';

import { KieFlowService } from './kie-flow.service';

describe('KieFlowService', () => {
  let service: KieFlowService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(KieFlowService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
