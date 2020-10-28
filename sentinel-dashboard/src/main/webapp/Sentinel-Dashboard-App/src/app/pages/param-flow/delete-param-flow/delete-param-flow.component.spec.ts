import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DeleteParamFlowComponent } from './delete-param-flow.component';

describe('DeleteParamFlowComponent', () => {
  let component: DeleteParamFlowComponent;
  let fixture: ComponentFixture<DeleteParamFlowComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DeleteParamFlowComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DeleteParamFlowComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
