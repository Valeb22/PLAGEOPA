// co.plageopa.DTO.ImportPreviewRow
package co.plageopa.DTO;

import java.util.ArrayList;
import java.util.List;

public class ImportPreviewRow {
  public int rowNum;                 // fila excel (1-based)
  public boolean ok;
  public List<String> errors = new ArrayList<>();

  public String cedula;
  public String nombre;
  public String telefono;
  public String genero;
  public String perteneceAsociacion; // SI/NO
  public String nombreAsociacion;

  public Double areaTotal;
  public String tipoActividad;
  public Double lon;
  public Double lat;
  public String codigoVereda;        // opcional

  public String nombreCultivo;
  public String variedad;
  public Double areaCultivo;

  public String globalid;            // puede venir vacío
}
