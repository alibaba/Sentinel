import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { UpdateParamFlowComponent } from './update-param-flow.component';

describe('UpdateParamFlowComponent', () => {
  let component: UpdateParamFlowComponent;
  let fixture: ComponentFixture<UpdateParamFlowComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ UpdateParamFlowComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UpdateParamFlowComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
