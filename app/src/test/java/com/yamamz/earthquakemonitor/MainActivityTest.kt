package com.yamamz.earthquakemonitor

import com.yamamz.earthquakemonitor.adapter.QuakeAdapter
import com.yamamz.earthquakemonitor.mainMVP.MainInteractor
import com.yamamz.earthquakemonitor.mainMVP.MainMVP
import com.yamamz.earthquakemonitor.mainMVP.MainPresenter
import com.yamamz.earthquakemonitor.model.EarthQuake
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito


/**
 * Created by AMRI on 11/15/2017.
 */

import org.mockito.Mockito.*
class MainActivityTest{

  @Mock
    var mainView :MainMVP.View=Mockito.mock(MainMVP.View::class.java)

@Mock
var context=Mockito.mock(MyApplication::class.java)

  @Mock
  var mainPresenter:MainMVP.Presenter=Mockito.mock(MainMVP.Presenter::class.java)

  @Mock
  val earthquakes=ArrayList<EarthQuake>()

    @Test
    fun shouldShowLoading(){
    val presenter=MainPresenter(mainView)
        presenter.showLoading()
    verify(mainView)?.showLoading()
    }

  @Test
  fun shouldStopLoading(){
    val presenter=MainPresenter(mainView)
    presenter.stopLoading()
    verify(mainView).stopLoading()
  }

  @Test
  fun shouldCheckRecyclerViewEmpty(){
    val adapter=Mockito.mock(QuakeAdapter::class.java)
    val presenter=MainPresenter(mainView)

    presenter.checkRecyclerViewIsemplty(adapter)
    verify(mainView).checkRecyclerViewIsemplty(adapter)

  }

  @Test
  fun shouldSetEarthquakeList(){
    val presenter=MainPresenter(mainView)
    presenter.setRecyclerView(earthquakes)
    verify(mainView).setEarthquakeOnList(earthquakes)
  }


@Test
fun shouldGotoDetails(){

  val presenter=MainPresenter(mainView)
  presenter.goToDeatails(0)
  verify(mainPresenter).goToDeatails(0)

}
}
