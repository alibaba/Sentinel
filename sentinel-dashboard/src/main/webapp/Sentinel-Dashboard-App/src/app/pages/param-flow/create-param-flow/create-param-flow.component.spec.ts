import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CreateParamFlowComponent } from './create-param-flow.component';

describe('CreateParamFlowComponent', () => {
  let component: CreateParamFlowComponent;
  let fixture: ComponentFixture<CreateParamFlowComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CreateParamFlowComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CreateParamFlowComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
