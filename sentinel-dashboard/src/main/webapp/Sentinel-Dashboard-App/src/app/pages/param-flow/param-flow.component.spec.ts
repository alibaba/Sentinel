import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ParamFlowComponent } from './param-flow.component';

describe('ParamFlowComponent', () => {
  let component: ParamFlowComponent;
  let fixture: ComponentFixture<ParamFlowComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ParamFlowComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ParamFlowComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
