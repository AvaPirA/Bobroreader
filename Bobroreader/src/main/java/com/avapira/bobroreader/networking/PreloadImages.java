/*
 * Bobroreader is open source software, created, maintained, and shared under
 * the MIT license by Avadend Piroserpen Arts. The project includes components
 * from other open source projects which remain under their existing licenses,
 * detailed in their respective source files.
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2015. Avadend Piroserpen Arts Ltd.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 *
 */

package com.avapira.bobroreader.networking;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class PreloadImages {

    //@formatter:off
    public static final String[] urls = {
            "/images/banners/ve/125699339172544.png",
            "/images/banners/ve/125698553182650.png",
            "/images/banners/ve/125698880498448.png",
            "/images/banners/vg/125701543238460.png",
            "/images/banners/vg/125718465081528.jpg",
            "/images/banners/vg/125725276436260.png",
            "/images/banners/vg/125752214930592.png",
            "/images/banners/vg/VG_paradox.png",
            "/images/banners/vg/125709977081546.png",
            "/images/banners/vg/125787756729250.png",
            "/images/banners/vg/vg-vvvvvv.png",
            "/images/banners/vg/vg.png",
            "/images/banners/vg/125860991758021.jpg",
            "/images/banners/vg/125701543235898.png",
            "/images/banners/vn/125861005475361.jpg",
            "/images/banners/gf/125860979571217.jpg",
            "/images/banners/news/125710395977840.png",
            "/images/banners/cr/cr2.png",
            "/images/banners/cr/cr4.png",
            "/images/banners/cr/cr_vampire.png",
            "/images/banners/cp/125788239756657.gif",
            "/images/banners/slow/slow-2-copy-new.png",
            "/images/banners/slow/slow-3.png",
            "/images/banners/slow/slow-4.png",
            "/images/banners/tv/46599021_1248208279_ClapperBoardManResized2.png",
            "/images/banners/tv/ccd7cbf4e061.png",
            "/images/banners/tv/3672202a3ac0.png",
            "/images/banners/tv/2bd020d5bb30.png",
            "/images/banners/tv/snapshot20100724100336.png",
            "/images/banners/tv/55555.png",
            "/images/banners/tv/2e04001fa57f.jpg",
            "/images/banners/lor/lor.png",
            "/images/banners/lor/125768117841327.png",
            "/images/banners/lor/125701543231992.png",
            "/images/banners/lor/125702504832252.png",
            "/images/banners/to/to-lunatic.png",
            "/images/banners/to/to-cirnotopter.png",
            "/images/banners/to/to-oppai-edition-(lewd).png",
            "/images/banners/to/to_cosplay.png",
            "/images/banners/to/125861045424732.jpg",
            "/images/banners/rf/125699252353074.png",
            "/images/banners/rf/125861026695515.jpg",
            "/images/banners/rf/125736226221120.png",
            "/images/banners/rf/125701647009375.png",
            "/images/banners/rf/125701163950149.png",
            "/images/banners/hau/125861045418626.jpg",
            "/images/banners/bg/125861578033434.jpg",
            "/images/banners/bg/125697224028122.jpg",
            "/images/banners/bg/bg.png",
            "/images/banners/di/125702135915554.png",
            "/images/banners/di/125860991769106.jpg",
            "/images/banners/di/125762259407262.png",
            "/images/banners/wn/125861005478345.jpg",
            "/images/banners/wn/125701591350076.jpg",
            "/images/banners/wh/125861075646865.jpg",
            "/images/banners/wh/125697147834527.png",
            "/images/banners/wh/wh1.jpg",
            "/images/banners/wh/wh2.jpg",
            "/images/banners/mad/mad.jpg",
            "/images/banners/fur/125861026701646.jpg",
            "/images/banners/dt/125697739438064.png",
            "/images/banners/a/125700436332204.png",
            "/images/banners/a/125761210590870.jpg",
            "/images/banners/a/125768405443972.png",
            "/images/banners/a/125701704962528.png",
            "/images/banners/a/125702195165767.png",
            "/images/banners/b/b1.png",
            "/images/banners/b/125700861425839.gif",
            "/images/banners/b/125707340465667.gif",
            "/images/banners/b/125860969605710.jpg",
            "/images/banners/ma/125860969613262.jpg",
            "/images/banners/ma/ma.png",
            "/images/banners/d/125711152029591.png",
            "/images/banners/d/d_motherland_hears_you.png",
            "/images/banners/sw/125861045421667.jpg",
            "/images/banners/sw/125694314851117.png",
            "/images/banners/mu/125758466249422.jpg",
            "/images/banners/mu/125706039606802.png",
            "/images/banners/mu/125701524833743.png",
            "/images/banners/mu/125758466251705.jpg",
            "/images/banners/mu/--116.png",
            "/images/banners/mu/125861048005976.jpg",
            "/images/banners/s/125776130692418.png",
            "/images/banners/s/125860969610249.jpg",
            "/images/banners/r/r.png",
            "/images/banners/r/125699732718180.png",
            "/images/banners/u/125860969598039.jpg",
            "/images/banners/u/125696941765285.png",
            "/images/banners/azu/125735086547830.png",
            "/images/banners/azu/125734932766150.png",
            "/images/banners/azu/125861045415565.jpg",
            "/images/banners/azu/azu.png"};
//    @formatter:on

    public static void main(String[] args) throws IOException, InterruptedException {
        final String OCURRENCER = "/images";
        System.out.println(urls[0].substring(urls[0].indexOf(OCURRENCER) + OCURRENCER.length() + 1)
                                  .toLowerCase()
                                  .replace('/', '_'));
    }

    private static void foo() throws MalformedURLException, InterruptedException {
        for (String imgUrl : urls) {
            URL url = new URL("http://dobrochan.ru".concat(imgUrl));
//            BufferedImage img = ImageIO.read(url);
            File outputFile = new File(imgUrlToProvidedImgNames(imgUrl));
//            String fmt = imgFileFullName.substring(1 + imgFileFullName.indexOf('.'));
//            ImageIO.write(img, fmt, outputFile);
            System.out.println("loaded " + outputFile);
            Thread.sleep(333);
//            System.out.println(outputFile.getAbsolutePath());
        }
    }

    private static String imgUrlToProvidedImgNames(String imgUrl) {
        return "res/drawable/banners_" + imgUrl.indexOf("/images/");
    }

}
