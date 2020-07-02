import {Component, OnInit, ViewChild} from '@angular/core';
import {MatTableDataSource} from '@angular/material/table';
import {MatSort} from '@angular/material/sort';
import {MatPaginator} from '@angular/material/paginator';
import {DataService} from '../../services/data/data.service';
import {MdsService} from '../../services/mds/mds.service';

@Component({
  selector: 'app-test-runs',
  templateUrl: './test-runs.component.html',
  styleUrls: ['./test-runs.component.css']
})
export class TestRunsComponent implements OnInit {
  displayedColumns: string[] = ['runId', 'runName', 'runStatus', 'createdOn'];
  dataSource: any;

  @ViewChild(MatPaginator, {static: false}) paginator:MatPaginator;
  
  @ViewChild(MatSort, {static: false}) sort: MatSort;
  
  constructor(private dataService: DataService) { }

  ngOnInit() { }

  applyFilter(value: string) {
    this.dataSource.filter = value.trim().toLocaleLowerCase();
  }

  getRuns(email: string) {
    this.dataService.getRuns(email).subscribe(
      response => {
        this.dataSource = new MatTableDataSource();
        this.dataSource.data = response;
        this.dataSource.paginator = this.paginator;
        this.dataSource.sort = this.sort;
        localStorage.setItem('runs', JSON.stringify(response));
      },
      error => window.alert(error)
    );
  }

}

