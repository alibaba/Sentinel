import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DeleteDegradeComponent } from './delete-degrade.component';

describe('DeleteDegradeComponent', () => {
  let component: DeleteDegradeComponent;
  let fixture: ComponentFixture<DeleteDegradeComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DeleteDegradeComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DeleteDegradeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
