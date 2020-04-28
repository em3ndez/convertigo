import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { IonicModule } from '@ionic/angular';
import { TranslateModule } from '@ngx-translate/core';
/*=c8o_ModuleTsImports*/
/*=c8o_PageImport*/

@NgModule({
  declarations: [/*Begin_c8o_NgDeclarations*/
	/*=c8o_PageName*/
  /*End_c8o_NgDeclarations*/],
  imports: [/*Begin_c8o_NgModules*/
  	CommonModule,
	FormsModule,
	IonicModule,
	TranslateModule.forChild(),
	//IonicPageModule.forChild(/*=c8o_PageName*/),
    RouterModule.forChild([
      {
        path: '',
        component: /*=c8o_PageName*/
      }
    ])
  /*End_c8o_NgModules*/],
  entryComponents: [/*Begin_c8o_NgComponents*/
  /*End_c8o_NgComponents*/],
  providers: [/*Begin_c8o_NgProviders*/
  /*End_c8o_NgProviders*/]
})
export class /*=c8o_PageModuleName*/ {}