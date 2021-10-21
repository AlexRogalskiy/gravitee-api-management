/*
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { sortBy } from 'lodash';
import { Meta, moduleMetadata } from '@storybook/angular';
import { Story } from '@storybook/angular/dist/ts3.9/client/preview/types-7-0';
import { action } from '@storybook/addon-actions';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MatTableModule } from '@angular/material/table';
import { MatSortModule } from '@angular/material/sort';

import { GioTableWrapperComponent, GioTableWrapperFilters } from './gio-table-wrapper.component';
import { GioTableWrapperModule } from './gio-table-wrapper.module';

export interface PeriodicElement {
  name: string;
  position: number;
  weight: number;
  symbol: string;
}

const ELEMENT_DATA: PeriodicElement[] = [
  { position: 1, name: 'Hydrogen', weight: 1.0079, symbol: 'H' },
  { position: 2, name: 'Helium', weight: 4.0026, symbol: 'He' },
  { position: 3, name: 'Lithium', weight: 6.941, symbol: 'Li' },
  { position: 4, name: 'Beryllium', weight: 9.0122, symbol: 'Be' },
  { position: 5, name: 'Boron', weight: 10.811, symbol: 'B' },
  { position: 6, name: 'Carbon', weight: 12.0107, symbol: 'C' },
  { position: 7, name: 'Nitrogen', weight: 14.0067, symbol: 'N' },
  { position: 8, name: 'Oxygen', weight: 15.9994, symbol: 'O' },
  { position: 9, name: 'Fluorine', weight: 18.9984, symbol: 'F' },
  { position: 10, name: 'Neon', weight: 20.1797, symbol: 'Ne' },
];

export default {
  title: 'Shared / Table wrapper',
  component: GioTableWrapperComponent,
  decorators: [
    moduleMetadata({
      imports: [BrowserAnimationsModule, GioTableWrapperModule, MatTableModule, MatSortModule],
    }),
  ],
} as Meta;

export const Default: Story = {
  render: (args) => ({
    template: `
    <div>
      <gio-table-wrapper [filters]="filters" (filtersChange)="filtersChange($event); _filters = $event" >

        <table
          style="width: 100%;"
          mat-table
          [dataSource]="filterDataSource(_filters)"
          matSort
        >
          <!-- Position Column -->
          <ng-container matColumnDef="position">
            <th mat-header-cell *matHeaderCellDef mat-sort-header> No. </th>
            <td mat-cell *matCellDef="let element"> {{element.position}} </td>
          </ng-container>
        
          <!-- Name Column -->
          <ng-container matColumnDef="name">
            <th mat-header-cell *matHeaderCellDef mat-sort-header> Name </th>
            <td mat-cell *matCellDef="let element"> {{element.name}} </td>
          </ng-container>
        
          <!-- Weight Column -->
          <ng-container matColumnDef="weight">
            <th mat-header-cell *matHeaderCellDef mat-sort-header> Weight </th>
            <td mat-cell *matCellDef="let element"> {{element.weight}} </td>
          </ng-container>
        
          <!-- Symbol Column -->
          <ng-container matColumnDef="symbol">
            <th mat-header-cell *matHeaderCellDef mat-sort-header> Symbol </th>
            <td mat-cell *matCellDef="let element"> {{element.symbol}} </td>
          </ng-container>

          <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
          <tr mat-row *matRowDef="let row; columns: displayedColumns"></tr>
      
          <tr class="mat-row" *matNoDataRow>
            <td class="mat-cell" [attr.colspan]="displayedColumns.length">No Data</td>
          </tr>
        </table>

      </gio-table-wrapper>
    </div>
    `,
    props: {
      filters: {
        ...(args.filterSearchTerm ? { searchTerm: args.filterSearchTerm } : {}),
        ...(args.filterPagination ? { pagination: args.filterPagination } : {}),
        ...(args.filterSort ? { sort: args.filterSort } : {}),
      },
      displayedColumns: ['position', 'name', 'weight', 'symbol'],
      // dumb function to simulate filters
      filterDataSource(filter: GioTableWrapperFilters) {
        let dataSource = ELEMENT_DATA;

        if (filter?.pagination) {
          dataSource = dataSource.slice(
            (filter.pagination.index - 1) * filter.pagination.size,
            filter.pagination.index * filter.pagination.size,
          );
        }

        if (filter?.searchTerm) {
          dataSource = dataSource.filter((element) => {
            return element.name.toLowerCase().includes(filter.searchTerm.toLowerCase());
          });
        }

        if (filter?.sort) {
          dataSource = sortBy(dataSource, filter.sort.active);

          if (filter.sort.direction === 'desc') {
            dataSource = dataSource.reverse();
          }
        }

        return dataSource;
      },
      filtersChange: action('filtersChange'),
    },
  }),
  argTypes: {
    filterSearchTerm: {
      control: {
        type: 'text',
        default: '',
      },
    },
    filterPagination: {
      control: {
        type: 'object',
      },
      defaultValue: {
        index: 1,
        size: 10,
        length: ELEMENT_DATA.length,
      },
    },
    filterSort: {
      control: {
        type: 'object',
      },
      defaultValue: {
        active: 'position',
        direction: 'asc',
      },
    },
  },
};